package com.backend.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BudgetAllocationRequest {

    @NotBlank(message = "Category is required")
    @Pattern(
    regexp = "SEEDS|FERTILIZERS|PESTICIDES|LABOUR|MACHINERY|CROP_ACTIVITY|IRRIGATION|OTHER",
    flags = Pattern.Flag.CASE_INSENSITIVE,
    message = "Invalid budget category"
    )
    private String category;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(
        value = "0.01",
        message = "Allocated amount must be greater than zero"
    )
    private BigDecimal allocatedAmount;

    @Size(max = 300, message = "Notes cannot exceed 300 characters")
    private String notes;
}
