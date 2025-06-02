package com.JSR.PharmaFlow.DTO;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    private String quantity;
    private BigDecimal unitPrice;
    // other fields if needed
}