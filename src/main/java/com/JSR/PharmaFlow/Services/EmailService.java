package com.JSR.PharmaFlow.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendPasswordResetEmail(String toEmail, String resetToken) {
//        try {
//            // ✅ THIS SHOULD BE FRONTEND URL (port 5173), NOT BACKEND (port 8080)
//            String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;
//
//            SimpleMailMessage message = getSimpleMailMessage(toEmail, resetLink);
//
//            mailSender.send(message);
//            log.info("Password reset email sent successfully to: {}", toEmail);
//        } catch (Exception e) {
//            log.error("Failed to send email to: {}", toEmail, e);
//            throw new RuntimeException("Failed to send email: " + e.getMessage());
//        }
//    }
//
//    private static SimpleMailMessage getSimpleMailMessage(String toEmail, String resetLink) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Password Reset Request - Pharmacy App");
//        message.setText(
//                "You requested a password reset for your Pharmacy App account.\n\n" +
//                        "Click the link below to reset your password:\n" +
//                        resetLink + "\n\n" +
//                        "This link will expire in 1 hour.\n\n" +
//                        "If you didn't request this, please ignore this email."
//        );
//        return message;
//    }
//}
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            log.info("🚀🚀🚀 EMAIL SERVICE: Starting to send email to: {}", toEmail);

            String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;
            log.info("🚀🚀🚀 Reset link: {}", resetLink);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("dasarbind269@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Pharmacy App");
            message.setText(
                    "You requested a password reset for your Pharmacy App account.\n\n" +
                            "Click the link below to reset your password:\n" +
                            resetLink + "\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request this, please ignore this email."
            );

            log.info("🚀🚀🚀 About to send email via JavaMailSender...");
            mailSender.send(message);
            log.info("✅✅✅ EMAIL SENT SUCCESSFULLY to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌❌❌ FAILED to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}