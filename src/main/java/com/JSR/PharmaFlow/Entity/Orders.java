

package com.JSR.PharmaFlow.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.JSR.PharmaFlow.Enums.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // "cod", "credit_card", "debit_card", etc.

    @Embedded
    private PaymentDetails paymentDetails; // All payment info goes here

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-items")
    private List<OrderItems> orderItemsList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-orders")
    private Users users;

    @Column(name = "user_name")
    private String userName; // Store user name for easy access in admin panel

//    @PrePersist
//    public void prePersist() {
//        if (this.createdAt == null) {
//            this.createdAt = Instant.now();
//        }
//
//        // Initialize payment details if null
//        if (this.paymentDetails == null) {
//            this.paymentDetails = new PaymentDetails();
//            this.paymentDetails.setAmount(this.totalPrice);
//            this.paymentDetails.setPaymentDate(Instant.now());
//
//            // Set default payment status based on payment method
//            if ("cod".equalsIgnoreCase(this.paymentMethod)) {
//                this.paymentDetails.setPaymentStatus("pending");
//                this.paymentDetails.setTransactionId("COD_" + this.id);
//            } else {
//                this.paymentDetails.setPaymentStatus("completed");
//                this.paymentDetails.setTransactionId("TXN_" + System.currentTimeMillis());
//            }
//        }
//    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }

        // Initialize payment details if null
        if (this.paymentDetails == null) {
            this.paymentDetails = new PaymentDetails();
            this.paymentDetails.setAmount(this.totalPrice);
            this.paymentDetails.setPaymentDate(Instant.now());

            // Set default payment status based on payment method
            if ("cod".equalsIgnoreCase(this.paymentMethod)) {
                this.paymentDetails.setPaymentStatus("pending");
                this.paymentDetails.setTransactionId("COD_" + this.id);
            } else {
                this.paymentDetails.setPaymentStatus("completed");
                this.paymentDetails.setTransactionId("TXN_" + System.currentTimeMillis());
            }
        }
    }
}