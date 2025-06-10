package com.JSR.PharmaFlow.DTO;

import java.math.BigDecimal;

public record OrderItemDTO(
         String quantity,
         BigDecimal unitPrice
) {
};
