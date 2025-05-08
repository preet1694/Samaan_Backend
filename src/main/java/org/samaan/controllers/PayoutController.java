package org.samaan.controllers;

import org.json.JSONObject;
import org.samaan.dto.PayoutRequestDTO;
import org.samaan.model.PayoutDetails;
import org.samaan.repositories.PayoutRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
@RequestMapping("/api/payout")
public class PayoutController {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @Value("${razorpayx.account}")
    private String razorpayAccountNumber;

    private final PayoutRepository payoutRepo;

    public PayoutController(PayoutRepository payoutRepo) {
        this.payoutRepo = payoutRepo;
    }

    @PostMapping("/send")
    public ResponseEntity<?> createPayout(@RequestBody PayoutRequestDTO dto) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(razorpayKey, razorpaySecret);

            // 1. Create Contact
            JSONObject contactRequest = new JSONObject();
            contactRequest.put("name", dto.getName());
            contactRequest.put("email", dto.getEmail());
            contactRequest.put("type", "employee");

            HttpEntity<String> contactEntity = new HttpEntity<>(contactRequest.toString(), headers);
            ResponseEntity<String> contactResponse = restTemplate.postForEntity("https://api.razorpay.com/v1/contacts", contactEntity, String.class);
            String contactId = new JSONObject(contactResponse.getBody()).getString("id");

            // 2. Create Fund Account
            JSONObject upiDetails = new JSONObject();
            upiDetails.put("address", dto.getUpi());

            JSONObject fundAccountRequest = new JSONObject();
            fundAccountRequest.put("contact_id", contactId);
            fundAccountRequest.put("account_type", "vpa");
            fundAccountRequest.put("vpa", upiDetails);

            HttpEntity<String> fundEntity = new HttpEntity<>(fundAccountRequest.toString(), headers);
            ResponseEntity<String> fundResponse = restTemplate.postForEntity("https://api.razorpay.com/v1/fund_accounts", fundEntity, String.class);
            String fundAccountId = new JSONObject(fundResponse.getBody()).getString("id");

            // 3. Create Payout
            JSONObject payoutRequest = new JSONObject();
            payoutRequest.put("account_number", razorpayAccountNumber);
            payoutRequest.put("fund_account_id", fundAccountId);
            payoutRequest.put("amount", dto.getAmount());
            payoutRequest.put("currency", "INR");
            payoutRequest.put("mode", "UPI");
            payoutRequest.put("purpose", "payout");
            payoutRequest.put("queue_if_low_balance", true);

            // Generate a consistent or random UUID for idempotency
            String idempotencyKey = UUID.randomUUID().toString(); // safer for one-time payouts
            // OR: String idempotencyKey = "payout-" + dto.getEmail() + "-" + dto.getAmount();

            HttpHeaders payoutHeaders = new HttpHeaders();
            payoutHeaders.setContentType(MediaType.APPLICATION_JSON);
            payoutHeaders.setBasicAuth(razorpayKey, razorpaySecret);
            payoutHeaders.set("X-Payout-Idempotency", idempotencyKey);

            HttpEntity<String> payoutEntity = new HttpEntity<>(payoutRequest.toString(), payoutHeaders);
            ResponseEntity<String> payoutResponse = restTemplate.postForEntity("https://api.razorpay.com/v1/payouts", payoutEntity, String.class);

            JSONObject payoutJson = new JSONObject(payoutResponse.getBody());
            String payoutId = payoutJson.getString("id");
            String status = payoutJson.getString("status");

            // 4. Save to DB
            PayoutDetails details = new PayoutDetails();
            details.setPayoutId(payoutId);
            details.setContactId(contactId);
            details.setFundAccountId(fundAccountId);
            details.setName(dto.getName());
            details.setEmail(dto.getEmail());
            details.setUpi(dto.getUpi());
            details.setAmount(dto.getAmount());
            details.setStatus(status);

            payoutRepo.save(details);

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payout failed: " + e.getMessage());
        }
    }
}
