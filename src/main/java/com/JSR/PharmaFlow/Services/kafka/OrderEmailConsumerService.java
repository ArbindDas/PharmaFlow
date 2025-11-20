package com.JSR.PharmaFlow.Services.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class OrderEmailConsumerService {

    private final EmailServiceTwo emailService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderEmailConsumerService(EmailServiceTwo emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    // Consumer for order confirmation emails
    @KafkaListener(topics = "order-confirmed-email", groupId = "order-email-service")
    public void consumeOrderConfirmation(String orderEventJson) {
        try {
            Map<String, Object> orderEvent = objectMapper.readValue(orderEventJson,
                    new TypeReference<Map<String, Object>>() {});

            String email = (String) orderEvent.get("userEmail");
            String userName = (String) orderEvent.get("userName");
            Long orderId = ((Number) orderEvent.get("orderId")).longValue();

            emailService.sendOrderConfirmationEmail(email, userName, orderId);
            log.info("Order confirmation email sent to: {} for order ID: {}", email, orderId);

        } catch (Exception e) {
            log.error("Failed to process order confirmation: {}", orderEventJson, e);
        }
    }

    // Consumer for status update emails
    @KafkaListener(topics = "order-status-update-email", groupId = "order-email-service")
    public void consumeStatusUpdate(String statusEventJson) {
        try {
            Map<String, Object> statusEvent = objectMapper.readValue(statusEventJson,
                    new TypeReference<Map<String, Object>>() {});

            String email = (String) statusEvent.get("userEmail");
            String userName = (String) statusEvent.get("userName");
            Long orderId = ((Number) statusEvent.get("orderId")).longValue();
            String newStatus = (String) statusEvent.get("newStatus");
            String oldStatus = (String) statusEvent.get("oldStatus");

            emailService.sendOrderStatusUpdateEmail(email, userName, orderId, oldStatus, newStatus);
            log.info("Status update email sent to: {} for order ID: {} - {} -> {}",
                    email, orderId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to process status update: {}", statusEventJson, e);
        }
    }

    // Consumer for delivery confirmation emails
    @KafkaListener(topics = "order-delivered-email", groupId = "order-email-service")
    public void consumeDeliveryConfirmation(String deliveryEventJson) {
        try {
            Map<String, Object> deliveryEvent = objectMapper.readValue(deliveryEventJson,
                    new TypeReference<Map<String, Object>>() {});

            String email = (String) deliveryEvent.get("userEmail");
            String userName = (String) deliveryEvent.get("userName");
            Long orderId = ((Number) deliveryEvent.get("orderId")).longValue();

            emailService.sendOrderDeliveredEmail(email, userName, orderId);
            log.info("Delivery confirmation email sent to: {} for order ID: {}", email, orderId);

        } catch (Exception e) {
            log.error("Failed to process delivery confirmation: {}", deliveryEventJson, e);
        }
    }
}