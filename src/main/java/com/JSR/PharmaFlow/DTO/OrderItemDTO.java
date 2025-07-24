package com.JSR.PharmaFlow.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderItemDTO{
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private  BigDecimal unitPrice;


}
