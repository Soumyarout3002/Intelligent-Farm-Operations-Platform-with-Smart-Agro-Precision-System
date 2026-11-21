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
public class DashboardAnalyticsResponse {

    private long totalFarms;
    private long totalPlots;
    private BigDecimal totalCultivatedArea;
    private long activeCropCycles;

    private BigDecimal totalHarvestQuantity;
    private BigDecimal averageYieldPerAcre;

    private BigDecimal totalPlannedBudget;
    private BigDecimal totalOperationalCost;
    private BigDecimal totalEquipmentCost;
    private BigDecimal totalLabourCost;
    private BigDecimal totalInventoryCost;

    // Revenue table abhi database mein nahi hai, isliye honest tracking ke liye
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private boolean revenueDataAvailable;

    private BigDecimal totalWaterUsedLiters;
    private BigDecimal totalMachineryHours;
}