package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YieldAnalyticsResponse {

    private String cropName;
    private String season;
    private long cropCycleCount;
    private BigDecimal totalCultivatedArea;
    private BigDecimal totalHarvestQuantity;
    private BigDecimal totalDamagedQuantity;
    private BigDecimal yieldPerAcre;
    private String quantityUnit;
}