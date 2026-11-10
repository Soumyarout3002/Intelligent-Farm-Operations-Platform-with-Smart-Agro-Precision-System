
package com.backend.dto.equipment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentUsageRequest {

    @NotNull
    private Long equipmentId;

    @NotNull
    private LocalDate usageDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal operatingHours;

    @DecimalMin(value = "0.00")
    private BigDecimal fuelConsumed;

    @DecimalMin(value = "0.00")
    private BigDecimal fuelCost;

    @DecimalMin(value = "0.00")
    private BigDecimal usageCost;

    @Size(max = 1000)
    private String notes;
}
