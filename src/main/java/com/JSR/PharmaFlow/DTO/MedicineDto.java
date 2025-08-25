package com.JSR.PharmaFlow.DTO;


import com.JSR.PharmaFlow.Enums.MedicineStatus;
import com.JSR.PharmaFlow.Enums.Status;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicineDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDate expiryDate;
    @NotBlank (message = "Image URL cannot be empty")  // or remove this if it's optional
    private String imageUrl;
    private MedicineStatus medicineStatus;
    private Long createdBy;
    private Instant createdAt;

}
