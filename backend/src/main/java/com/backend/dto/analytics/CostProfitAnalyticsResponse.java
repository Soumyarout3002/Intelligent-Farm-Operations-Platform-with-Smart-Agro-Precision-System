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
public class CostProfitAnalyticsResponse {

    private String cropName;
    private String season;
    private long cropCycleCount;

    private BigDecimal plannedBudget;
    private BigDecimal equipmentCost;
    private BigDecimal labourCost;
    private BigDecimal inventoryCost;
    private BigDecimal otherActivityCost;
    private BigDecimal totalCost;

    private BigDecimal budgetVariance;
    private BigDecimal budgetUtilizationPercent;

    // Revenue/Sales entity database mein nahi hai, isliye honest tracking
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private BigDecimal profitMarginPercent;
    private boolean revenueDataAvailable;
}