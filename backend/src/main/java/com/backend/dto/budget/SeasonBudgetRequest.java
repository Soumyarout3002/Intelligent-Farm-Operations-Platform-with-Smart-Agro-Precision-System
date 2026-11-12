
package com.backend.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeasonBudgetRequest {

    @NotNull(message = "Crop cycle ID is required")
    private Long cropCycleId;

    @NotNull(message = "Total budget is required")
    @DecimalMin(
        value = "0.01",
        message = "Total budget must be greater than zero"
    )
    private BigDecimal totalBudget;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
