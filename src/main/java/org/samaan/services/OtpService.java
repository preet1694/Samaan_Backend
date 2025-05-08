package org.samaan.services;

import org.samaan.model.OtpStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;
    private final Map<String, OtpStorage> otpMap = new ConcurrentHashMap<>();

    @Autowired
    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
        otpMap.put(email, new OtpStorage(otp, expiryTime));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + ". It is valid for 5 minutes.");
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpMap.containsKey(email)) return false;

        OtpStorage stored = otpMap.get(email);
        if (stored.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpMap.remove(email);
            return false;
        }

        boolean isValid = stored.getOtp().equals(otp);
        if (isValid) otpMap.remove(email); // Invalidate after use

        return isValid;
    }
}

