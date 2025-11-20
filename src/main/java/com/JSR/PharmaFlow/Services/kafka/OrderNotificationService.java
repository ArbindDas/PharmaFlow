package com.JSR.PharmaFlow.Services.kafka;


import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Service
@Slf4j
public class OrderNotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UsersRepository usersRepository;

    @Autowired
    public OrderNotificationService(KafkaTemplate<String, Object> kafkaTemplate,
                                    UsersRepository usersRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.usersRepository = usersRepository;
        log.info("✅ OrderNotificationService initialized with KafkaTemplate");
    }

    public void sendOrderConfirmation(Long orderId, String userEmail) {
        try {
            log.info("📧 Preparing order confirmation for order ID: {}, user: {}", orderId, userEmail);

            Users user = usersRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

            Map<String, Object> orderEvent = new HashMap<>();
            orderEvent.put("orderId", orderId);
            orderEvent.put("userEmail", userEmail);
            orderEvent.put("userName", user.getFullName());
            orderEvent.put("type", "ORDER_CONFIRMED");
            orderEvent.put("timestamp", Instant.now().toString());

            log.info("📤 Sending Kafka message to topic: order-confirmed-email");

            // Simple send without callback (compatible with older Spring Kafka)
            kafkaTemplate.send("order-confirmed-email", userEmail, orderEvent);

            log.info("✅ Order confirmation event sent to Kafka for order ID: {}", orderId);

        } catch (Exception e) {
            log.error("❌ Failed to send order confirmation for order ID: {}", orderId, e);
            throw e; // Re-throw to see in controller
        }
    }

    // Send order status update email
    public void sendStatusUpdate(Long orderId, String userEmail, String oldStatus, String newStatus) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Map<String, Object> statusEvent = new HashMap<>();
        statusEvent.put("orderId", orderId);
        statusEvent.put("userEmail", userEmail);
        statusEvent.put("userName", user.getFullName());
        statusEvent.put("oldStatus", oldStatus);
        statusEvent.put("newStatus", newStatus);
        statusEvent.put("type", "STATUS_UPDATED");
        statusEvent.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("order-status-update-email", userEmail, statusEvent);
        log.info("Status update event sent for order ID: {} - {} -> {}", orderId, oldStatus, newStatus);
    }

    // Send delivery confirmation email
    public void sendDeliveryConfirmation(Long orderId, String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Map<String, Object> deliveryEvent = new HashMap<>();
        deliveryEvent.put("orderId", orderId);
        deliveryEvent.put("userEmail", userEmail);
        deliveryEvent.put("userName", user.getFullName());
        deliveryEvent.put("type", "ORDER_DELIVERED");
        deliveryEvent.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("order-delivered-email", userEmail, deliveryEvent);
        log.info("Delivery confirmation event sent for order ID: {}", orderId);
    }
}