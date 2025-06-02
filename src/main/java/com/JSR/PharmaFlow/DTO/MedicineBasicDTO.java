package com.JSR.PharmaFlow.DTO;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class MedicineBasicDTO {
    private Long id;
    private String name;
    private BigDecimal price;
}
