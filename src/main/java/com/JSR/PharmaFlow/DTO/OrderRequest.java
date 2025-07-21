package com.JSR.PharmaFlow.DTO;


import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private double totalPrice;
    private List<OrderItemDto> orderItems;

    @Data
    public static class OrderItemDto {

        private Long medicineId;
        private String quantity;
        private double unitPrice;
    }
}