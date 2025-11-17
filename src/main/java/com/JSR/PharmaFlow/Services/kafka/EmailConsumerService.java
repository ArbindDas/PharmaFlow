package com.JSR.PharmaFlow.Services.kafka;

import com.JSR.PharmaFlow.Services.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class EmailConsumerService {

    private EmailServiceTwo emailService;

    @Autowired
    public EmailConsumerService(EmailServiceTwo emailService) {
        this.emailService = emailService;
    }


    @KafkaListener(topics = "user-welcome-email", groupId = "email-service")
    public void consumeWelcomeEmail(Map<String , Object> welcomeEvent){


        try {
            String email = (String) welcomeEvent.get("email");
            String username = (String)  welcomeEvent.get("username");

            // send welcome email
            emailService.sendWelcomeEmail(email , username);
            log.info("Welcome email sent successfully to: {}", email);
        } catch (RuntimeException | MessagingException e) {
            log.error("Failed to process welcome email for event: {}", welcomeEvent, e);
        }
    }
}
