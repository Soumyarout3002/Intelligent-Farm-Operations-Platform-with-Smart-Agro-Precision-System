
package com.backend.dto.equipment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentUsageResponse {

    private Long id;
    private Long equipmentId;
    private String equipment;
    private Long recordedById;
    private String recordedBy;
    private LocalDate usageDate;
    private BigDecimal operatingHours;
    private BigDecimal fuelConsumed;
    private BigDecimal fuelCost;
    private BigDecimal usageCost;
    private BigDecimal totalCost;
    private String notes;
    private LocalDateTime createdAt;
}
