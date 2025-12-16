package com.JSR.PharmaFlow.Entity;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class PaymentDetails {
    private String paymentStatus; // "pending", "completed", "failed", "refunded"
    private BigDecimal amount;
    private Instant paymentDate;
    private String transactionId; // For Stripe or other payment gateways
    private String paymentIntentId; // Stripe payment intent ID
    private String stripePaymentId; // Stripe payment ID
    private String stripeCustomerId; // Stripe customer ID
}
