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
public class WaterUsageAnalyticsResponse {

    private BigDecimal totalWaterUsedLiters;
    private long totalSchedules;
    private long completedSchedules;
    private BigDecimal averageWaterPerSchedule;
    private BigDecimal averageWaterPerDay;

    private List<FarmWaterUsage> farmBreakdown;
    private List<DailyWaterUsage> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmWaterUsage {
        private Long farmId;
        private String farmName;
        private BigDecimal totalWaterLiters;
        private long scheduleCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyWaterUsage {
        private LocalDate date;
        private BigDecimal totalWaterLiters;
        private long scheduleCount;
    }
}