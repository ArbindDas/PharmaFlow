
package com.JSR.PharmaFlow.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicines medicine;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order-items")
    private Orders orders;

    public OrderItems(Integer quantity, BigDecimal unitPrice, Medicines medicine, Orders order) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.medicine = medicine;
        this.orders = order;
    }

    public OrderItems(Integer quantity, BigDecimal unitPrice, Orders order) {

    }


    public void setTotalPrice(BigDecimal multiply) {

    }
}