
package com.backend.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private LocalDate expiryDate;
}
