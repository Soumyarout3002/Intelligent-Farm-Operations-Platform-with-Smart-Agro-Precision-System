package com.backend.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementRequest {

    @NotNull(message = "Movement quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2,
            message = "Quantity can have up to 10 integer digits and 2 decimal places")
    private BigDecimal quantity;

    private Long supplierId;

    private Long warehouseId;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // Purchase price per unit (required only for stock IN)
    @DecimalMin(
            value = "0.01",
            message = "Unit cost must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Unit cost can have up to 10 integer digits and 2 decimal places"
    )
    private BigDecimal unitCost;
}