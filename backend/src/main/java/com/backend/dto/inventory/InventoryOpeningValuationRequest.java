
package com.backend.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOpeningValuationRequest {

    @NotNull(message = "Opening quantity is required")
    @DecimalMin(value = "0.01",
            message = "Opening quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2,
            message = "Opening quantity can have up to 10 integer digits and 2 decimal places")
    private BigDecimal openingQuantity;

    @NotNull(message = "Opening unit cost is required")
    @DecimalMin(value = "0.01",
            message = "Opening unit cost must be greater than zero")
    @Digits(integer = 10, fraction = 2,
            message = "Opening unit cost can have up to 10 integer digits and 2 decimal places")
    private BigDecimal openingUnitCost;
}
