package com.JSR.PharmaFlow.DTO;


import com.JSR.PharmaFlow.Enums.Status;
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
    private String imageUrl;
    private Status status;
    private Long createdBy;
    private Instant createdAt;

}
