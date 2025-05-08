package org.samaan.controllers;

import org.samaan.dto.OtpRequestDTO;
import org.samaan.dto.OtpVerifyDTO;
import org.samaan.services.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequestDTO request) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent to email.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyDTO request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return isValid
                ? ResponseEntity.ok("OTP verified successfully.")
                : ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }
}

