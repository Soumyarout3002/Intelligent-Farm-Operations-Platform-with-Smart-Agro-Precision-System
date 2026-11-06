package com.backend.dto.inventory;

import com.backend.entity.StockMovement.MovementType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponse {

    private Long id;

    private Long farmId;

    private Long inventoryItemId;

    private String itemName;

    private MovementType type;

    private BigDecimal quantity;

    private BigDecimal availableQuantity;

    private Long supplierId;

    private String supplierName;

    private Long warehouseId;

    private String warehouseName;

    private String remarks;

    private LocalDateTime createdAt;
}