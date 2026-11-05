
package com.backend.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "Seeds|Fertilizers|Pesticides|Tools|Other",
            message = "Invalid inventory category"
    )
    private String category;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00", message = "Quantity cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Size(max = 20)
    private String unit;

    @NotNull(message = "Minimum stock is required")
    @DecimalMin(value = "0.00", message = "Minimum stock cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal minimum;
}
