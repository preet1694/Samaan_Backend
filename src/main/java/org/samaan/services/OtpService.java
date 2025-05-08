package org.samaan.services;

import jakarta.mail.internet.MimeMessage;
import org.samaan.model.OtpStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Your OTP Code");

            String htmlContent = """
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                            <h2 style="color: #4F46E5;">One-Time Password (OTP)</h2>
                            <p>Hello,</p>
                            <p>Your OTP is:</p>
                            <div style="font-size: 24px; font-weight: bold; background: #f3f4f6; padding: 10px 20px; display: inline-block; border-radius: 5px; color: #111827;">
                                %s
                            </div>
                            <p style="margin-top: 20px;">This OTP is valid for <strong>5 minutes</strong>. Please do not share it with anyone.</p>
                            <p style="color: #6b7280; font-size: 12px; margin-top: 30px;">If you did not request this code, please ignore this message.</p>
                        </div>
                    """.formatted(otp);

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle logging or fallback
        }
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

