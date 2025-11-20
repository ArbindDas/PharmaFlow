package com.JSR.PharmaFlow.Services.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class EmailConsumerService {

    private final EmailServiceTwo emailService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmailConsumerService(EmailServiceTwo emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    // This is your CONSUMER
    @KafkaListener(topics = "user-welcome-email", groupId = "email-service")
    public void consumeWelcomeEmail(String welcomeEventJson) {
        try {
            // Parse JSON string to Map
            Map<String, Object> welcomeEvent;
            // AUTOMATICALLY CALLED when message arrives in "user-welcome-email" topic
            welcomeEvent = objectMapper.readValue(welcomeEventJson, new TypeReference<Map<String, Object>>() {});

            String email = (String) welcomeEvent.get("email");
            String username = (String) welcomeEvent.get("username");

            if (email == null || username == null) {
                log.error("Invalid welcome event: missing email or username");
                return;
            }

            emailService.sendWelcomeEmail(email, username);
            log.info("Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to process welcome email for event: {}", welcomeEventJson, e);
        }
    }
}