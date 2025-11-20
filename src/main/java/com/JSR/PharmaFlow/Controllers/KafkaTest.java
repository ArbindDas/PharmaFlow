package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.Services.kafka.EmailServiceTwo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
public class KafkaTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTest.class);

    @Autowired
    private EmailServiceTwo emailServiceTwo;

    @PostMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        logger.info("Received request to send test email to: {}", email);

        try {
            // Validate email format
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email parameter is required");
            }

            emailServiceTwo.sendWelcomeEmail(email, "Test User");
            logger.info("Test email sent successfully to: {}", email);
            return ResponseEntity.ok("Test email sent successfully to: " + email);
        } catch (Exception e) {
            logger.error("Failed to send test email to {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("Failed to send test email: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint accessed");
        return ResponseEntity.ok("Jai Shree Ram, it's working....");
    }
}