package com.JSR.PharmaFlow.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class OrderRequest {
    private double totalPrice;
    private List<OrderItemDto> orderItems;
    @Getter
    @Setter
    private String paymentMethod; // Add this field
    private String paymentIntentId; // Add this field


    @Data
    public static class OrderItemDto {

        private Long medicineId;
        private String quantity;
        private double unitPrice;
    }

}