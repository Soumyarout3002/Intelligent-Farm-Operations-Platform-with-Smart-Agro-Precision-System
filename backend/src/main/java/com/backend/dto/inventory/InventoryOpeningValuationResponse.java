
package com.backend.dto.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOpeningValuationResponse {

    private Long id;

    private Long inventoryItemId;
    private String itemName;

    private Long warehouseId;
    private String warehouseName;

    private BigDecimal openingQuantity;
    private BigDecimal openingUnitCost;
    private BigDecimal openingTotalCost;

    private LocalDateTime createdAt;
}
