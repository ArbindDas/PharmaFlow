package com.JSR.PharmaFlow.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private BigDecimal unitPrice;

}
