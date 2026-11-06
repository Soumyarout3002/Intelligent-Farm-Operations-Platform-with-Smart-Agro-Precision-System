package com.backend.dto.crop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HarvestRequest {

    @NotNull
    private LocalDate harvestDate;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    @NotBlank
    @Size(max = 30)
    private String quantityUnit;

    @Size(max = 30)
    private String qualityGrade;

    @DecimalMin("0.0")
    private BigDecimal damagedQuantity;

    private String notes;
}