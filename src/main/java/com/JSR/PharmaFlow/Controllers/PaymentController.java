package com.JSR.PharmaFlow.Controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class PaymentController {

    @Value("${stripe.secret.key}") // Default to empty string if not found
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && ! stripeSecretKey.isEmpty()) {
            Stripe.apiKey = stripeSecretKey;
        } else {
            System.err.println("WARNING: Stripe secret key not configured. Payment functionality will not work.");
        }
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentRequest request) {
        // Check if Stripe is configured
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Stripe payment is not configured on the server");
            return ResponseEntity.status(503).body(error);
        }

        // Validate amount (now in Rupees)
        if (request.getAmount() == null || request.getAmount() < 0.5) { // ₹0.50 minimum
            Map<String, String> error = new HashMap<>();
            error.put("error", "Amount must be at least ₹0.50 INR");
            error.put("minimumAmount", "0.5");
            error.put("currency", "inr");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Convert Rupees to paise (multiply by 100)
            long amountInPaise = (long) (request.getAmount() * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise) // Use converted amount
                    .setCurrency("inr")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stripeErrorCode", e.getCode());
            return ResponseEntity.status(500).body(error);
        }
    }

    @Setter
    @Getter
    public static class CreatePaymentRequest {
        private Double amount; // Change from Long to Double to accept decimal values
    }
}