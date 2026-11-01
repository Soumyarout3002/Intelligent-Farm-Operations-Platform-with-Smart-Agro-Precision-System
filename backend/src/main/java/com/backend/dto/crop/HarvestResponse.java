package com.backend.dto.crop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HarvestResponse {

    private Long id;
    private Long cropCycleId;
    private String cropName;
    private LocalDate harvestDate;
    private BigDecimal quantity;
    private String quantityUnit;
    private String qualityGrade;
    private BigDecimal damagedQuantity;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}