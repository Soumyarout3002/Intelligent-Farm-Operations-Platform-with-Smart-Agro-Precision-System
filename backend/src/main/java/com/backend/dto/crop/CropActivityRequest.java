
package com.backend.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CropActivityRequest {

    @NotBlank(message = "Activity type is required")
    @Size(max = 50)
    private String activityType;

    @NotBlank(message = "Activity title is required")
    @Size(max = 150)
    private String title;

    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    private String description;

    private String quantity;

    private String performedBy;

    // Inventory item used during crop activity
    private Long inventoryItemId;

    // Warehouse from which inventory will be consumed
    private Long warehouseId;

    // Actual quantity consumed from inventory
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Used quantity must be greater than zero")
    private BigDecimal usedQuantity;
}
