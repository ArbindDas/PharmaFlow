package com.JSR.PharmaFlow.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {

    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;
    private MedicineBasicDTO medicine;
}
