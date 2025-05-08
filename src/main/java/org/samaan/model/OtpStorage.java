package org.samaan.model;

import java.time.LocalDateTime;

public class OtpStorage {
    private String otp;
    private LocalDateTime expiryTime;

    public OtpStorage(String otp, LocalDateTime expiryTime) {
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getOtp() {
        return otp;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
}

