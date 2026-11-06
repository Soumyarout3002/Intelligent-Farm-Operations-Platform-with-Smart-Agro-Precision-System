package com.backend.dto.inventory;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockResponse {

    private Long id;

    private Long inventoryItemId;
    private String itemName;
    private String category;

    private BigDecimal quantity;
    private String unit;

    private Long warehouseId;
    private String warehouseName;
}