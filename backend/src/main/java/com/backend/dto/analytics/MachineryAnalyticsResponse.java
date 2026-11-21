package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineryAnalyticsResponse {

    private BigDecimal totalOperatingHours;
    private BigDecimal totalFuelConsumed;
    private BigDecimal totalMachineryCost;
    private long totalUsageRecords;
    private BigDecimal averageHoursPerRecord;

    private List<MachineUsageSummary> machineBreakdown;
    private List<DailyMachineryUsage> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MachineUsageSummary {
        private Long equipmentId;
        private String equipmentName;
        private String equipmentType;
        private String farmName;
        private BigDecimal totalOperatingHours;
        private BigDecimal totalFuelConsumed;
        private BigDecimal totalCost;
        private long usageCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyMachineryUsage {
        private LocalDate date;
        private BigDecimal totalOperatingHours;
        private BigDecimal totalFuelConsumed;
        private BigDecimal totalCost;
    }
}