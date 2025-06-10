package com.JSR.PharmaFlow.DTO;

import java.math.BigDecimal;

public record OrderItemResponseDto(
         Long id,
         Integer quantity,
         BigDecimal unitPrice,
         MedicineBasicDto medicine
) {
};
