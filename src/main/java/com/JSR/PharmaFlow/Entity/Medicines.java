package com.JSR.PharmaFlow.Entity;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.JSR.PharmaFlow.Enums.MedicineStatus;
import com.JSR.PharmaFlow.Enums.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (
        name = "medicines"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

    public class Medicines {

        @Id
        @GeneratedValue(
                strategy = GenerationType.IDENTITY
        )
        private Long id;


        @NotBlank(message = "Medicine name cannot be empty")
        @Size(min = 2, max = 100, message = "Medicine name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\- ]+$", message = "Medicine name must only contain letters, numbers, hyphens and spaces")
        @Column(name = "name", nullable = false)
        private String name;


        @NotBlank(message = "Description cannot be empty")
        @Size(max = 1000, message = "Description is too long")
        @Column(name = "description", nullable = false)
        private String description;

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must be a valid decimal number")
        @Column(name = "price", nullable = false)
        private BigDecimal price;


        @NotNull (message = "Stock cannot be null")
        @Min (value = 0, message = "Stock cannot be negative")
        @Column(name = "stock", nullable = false)
        private Integer stock;

        @NotNull(message = "Expiry date is required")
        @Future (message = "Expiry date must be in the future")
        @Column(name = "expiry_date", nullable = false)
        private LocalDate expiryDate;

        @NotBlank (message = "Image URL cannot be empty")
        @Size(max = 2048, message = "Image URL is too long")
        @Pattern (regexp = "^(http|https)://.*\\.(jpg|jpeg|png|gif|webp)$", message = "Image URL must be a valid image URL")
        @Column(name = "image_url", nullable = false)
        private String imageUrl;


        @Enumerated(EnumType.STRING)
        @NotNull(message="Medicine status must be defined")
        @Column(name="medicineStatus",nullable=false)
        private MedicineStatus medicineStatus;


        @Column(name = "created_at", nullable = false)
        private Instant createdAt;


        @PrePersist
        public void prePersist() {
            if (this.createdAt == null) {
                this.createdAt = Instant.now();
            }
        }

    }
