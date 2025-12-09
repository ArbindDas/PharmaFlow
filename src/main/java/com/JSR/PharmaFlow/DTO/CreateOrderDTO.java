package com.JSR.PharmaFlow.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {

    private BigDecimal totalPrice;
    private String paymentMethod;
    private String paymentIntentId;  // For Stripe payments
    private List<OrderItemDTO> orderItems;



    @Data
    public static  class OrderItemDTO{

        private Long medicineId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

}


