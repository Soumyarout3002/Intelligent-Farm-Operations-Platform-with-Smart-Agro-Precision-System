
package com.backend.service;

import com.backend.dto.inventory.InventoryOpeningValuationRequest;
import com.backend.dto.inventory.InventoryOpeningValuationResponse;
import com.backend.entity.*;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryOpeningValuationService {

    private final FarmRepository farmRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryOpeningValuationRepository
            openingValuationRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryOpeningValuationResponse createOpeningValuation(
            Long farmId,
            Long itemId,
            Long warehouseId,
            Long ownerId,
            InventoryOpeningValuationRequest request) {

        // 1. Verify farm ownership
        farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        // 2. Verify inventory item belongs to farm
        InventoryItem item = inventoryItemRepository
                .findByIdAndFarmIdForUpdate(itemId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inventory item not found"));

        // 3. Verify warehouse belongs to farm
        Warehouse warehouse = warehouseRepository
                .findByIdAndFarmId(warehouseId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Warehouse not found"));

        // 4. Prevent duplicate opening valuation
        if (openingValuationRepository
                .existsByInventoryItemIdAndWarehouseId(
                        itemId, warehouseId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Opening valuation already exists for this item and warehouse");
        }
        // 5. Prevent opening valuation after stock movements
        if (stockMovementRepository
                .existsByInventoryItemIdAndWarehouseId(
                        itemId, warehouseId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot create opening valuation because stock movements "
                            + "already exist for this item and warehouse");
        }
        // 5. Fetch existing warehouse stock
        InventoryStock stock = inventoryStockRepository
                .findByItemAndWarehouseForUpdate(itemId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse stock not found for this inventory item"));

        // 6. Validate opening quantity against actual stock
        if (request.getOpeningQuantity().compareTo(
                stock.getQuantity()) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Opening quantity must match current warehouse stock: "
                            + stock.getQuantity());
        }

        // 7. Calculate opening total cost
        BigDecimal totalCost = request.getOpeningQuantity()
                .multiply(request.getOpeningUnitCost())
                .setScale(2, RoundingMode.HALF_UP);

        // 8. Initialize warehouse stock valuation
        stock.setAverageUnitCost(
                request.getOpeningUnitCost()
                        .setScale(2, RoundingMode.HALF_UP)
        );

        stock.setStockValue(totalCost);

        inventoryStockRepository.save(stock);

        // 8. Save opening valuation
        InventoryOpeningValuation valuation =
                InventoryOpeningValuation.builder()
                        .inventoryItem(item)
                        .warehouse(warehouse)
                        .openingQuantity(request.getOpeningQuantity())
                        .openingUnitCost(request.getOpeningUnitCost())
                        .openingTotalCost(totalCost)
                        .build();

        InventoryOpeningValuation saved =
                openingValuationRepository.save(valuation);

        return toResponse(saved);
    }

    private InventoryOpeningValuationResponse toResponse(
            InventoryOpeningValuation valuation) {

        return InventoryOpeningValuationResponse.builder()
                .id(valuation.getId())
                .inventoryItemId(
                        valuation.getInventoryItem().getId())
                .itemName(
                        valuation.getInventoryItem().getName())
                .warehouseId(
                        valuation.getWarehouse().getId())
                .warehouseName(
                        valuation.getWarehouse().getName())
                .openingQuantity(valuation.getOpeningQuantity())
                .openingUnitCost(valuation.getOpeningUnitCost())
                .openingTotalCost(valuation.getOpeningTotalCost())
                .createdAt(valuation.getCreatedAt())
                .build();
    }
}
