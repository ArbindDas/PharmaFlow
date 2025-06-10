package com.JSR.PharmaFlow.DTO;

import java.math.BigDecimal;

public record MedicineBasicDto(
        Long id,
        String name,
        BigDecimal price
) {
};
