package org.samaan.controllers;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.samaan.model.PaymentDetails;
import org.samaan.model.Trip;
import org.samaan.repositories.PaymentRepository;
import org.samaan.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private final TripRepository tripRepository;

    @Autowired
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentController(TripRepository tripRepository, PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.tripRepository = tripRepository;
    }

    @PostMapping("/create-order/{tripId}")
    public ResponseEntity<?> createPaymentOrder(@PathVariable String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isEmpty()) return ResponseEntity.badRequest().body("Trip not found");

        Trip trip = optionalTrip.get();

        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

            int amountInPaise = (int) (trip.getPrice() * 100); // Razorpay uses paise

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "trip_" + tripId);
            orderRequest.put("payment_capture", 1); // 1 = auto-capture payment

            Order order = client.orders.create(orderRequest);


            // Return order details to frontend
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", amountInPaise);
            response.put("currency", "INR");
            response.put("key", razorpayKey); // Send public key to frontend
            response.put("tripId", tripId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> payload) {
        System.out.println("Received payload: " + payload);  // 🔍 Debug print

        String tripId = payload.get("tripId");
        String orderId = payload.get("razorpayOrderId");
        String paymentId = payload.get("razorpayPaymentId");
        String signature = payload.get("razorpaySignature");

        System.out.println("tripId: " + tripId);
        System.out.println("orderId: " + orderId);
        System.out.println("paymentId: " + paymentId);
        System.out.println("signature: " + signature);

        if (tripId == null || orderId == null || paymentId == null || signature == null) {
            return ResponseEntity.badRequest().body("Missing one or more required fields in the payload");
        }

        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isEmpty()) return ResponseEntity.badRequest().body("Trip not found");

        Trip trip = optionalTrip.get();

        try {
            String generatedSignature = hmacSHA256(orderId + "|" + paymentId, razorpaySecret);
            if (!generatedSignature.equals(signature)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payment signature");
            }

            PaymentDetails payment = new PaymentDetails();
            payment.setOrderId(orderId);
            payment.setPaymentId(paymentId);
            payment.setTripId(tripId);
            payment.setStatus("SUCCESS");
            payment.setPaidAmount(trip.getPrice());
            payment.setSenderEmail(trip.getSenderEmail());
            paymentRepository.save(payment);
            trip.setPaid(true);
            trip.setPaidAmount(trip.getPrice());
            trip.setRazorpayPaymentId(paymentId);
            tripRepository.save(trip);
            trip.setPaymentDate(LocalDate.now().toString());
            return ResponseEntity.ok("Payment verified successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Verification failed: " + e.getMessage());
        }
    }


    private String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());

        // Convert to hex (not Base64)
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
