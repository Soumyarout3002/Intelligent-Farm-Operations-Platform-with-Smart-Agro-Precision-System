package com.backend.service;

import com.backend.dto.inventory.StockMovementRequest;
import com.backend.dto.inventory.StockMovementResponse;
import com.backend.entity.*;
import com.backend.entity.StockMovement.MovementType;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final FarmRepository farmRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    public StockMovementResponse stockIn(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
        farmId, itemId, userId, request,
        MovementType.IN, null, null, request.getWarehouseId());
    }

    public StockMovementResponse stockOut(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
        farmId, itemId, userId, request,
        MovementType.OUT, null, null, request.getWarehouseId());
    }

    public StockMovementResponse stockOutForCropUsage(
        Long farmId,
        Long itemId,
        Long userId,
        Long warehouseId,
        CropActivity cropActivity,
        BigDecimal usedQuantity) {

    if (usedQuantity == null
            || usedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Used quantity must be greater than zero");
    }

    if (cropActivity == null
            || cropActivity.getCropCycle() == null
            || cropActivity.getCropCycle().getPlot() == null
            || cropActivity.getCropCycle().getPlot().getFarm() == null
            || !farmId.equals(
                    cropActivity.getCropCycle()
                            .getPlot().getFarm().getId())) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Crop activity does not belong to this farm");
    }

    return processMovement(
        farmId,
        itemId,
        userId,
        null,
        MovementType.OUT,
        cropActivity,
        usedQuantity,
        warehouseId);
}
    public boolean hasMovementForCropActivity(Long cropActivityId) {
    return stockMovementRepository
            .existsByCropActivityId(cropActivityId);
}

private InventoryStock getOrCreateWarehouseStock(
        InventoryItem item,
        Warehouse warehouse) {

    return inventoryStockRepository
            .findByItemAndWarehouseForUpdate(
                    item.getId(),
                    warehouse.getId())
            .orElseGet(() -> {
                InventoryStock stock = InventoryStock.builder()
                        .inventoryItem(item)
                        .warehouse(warehouse)
                        .quantity(BigDecimal.ZERO)
                        .build();

                return inventoryStockRepository.saveAndFlush(stock);
            });
}

    private StockMovementResponse processMovement(
        Long farmId,
        Long itemId,
        Long userId,
        StockMovementRequest request,
        MovementType type,
        CropActivity cropActivity,
        BigDecimal overrideQuantity,
        Long warehouseId) {

    Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Farm not found"));

    InventoryItem item = inventoryItemRepository
            .findByIdAndFarmIdForUpdate(itemId, farmId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Inventory item not found"));

    BigDecimal movementQuantity = overrideQuantity != null
            ? overrideQuantity
            : request.getQuantity();
//1
        BigDecimal unitCost = null;
        BigDecimal totalCost = null;
        BigDecimal valuedQuantityConsumed = BigDecimal.ZERO;
        BigDecimal unvaluedQuantityConsumed = BigDecimal.ZERO;

        if (type == MovementType.IN) {

        if (request.getUnitCost() == null
                || request.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unit cost is required for stock IN"
                );
        }

        // Purchase cost for this stock IN movement
        unitCost = request.getUnitCost()
                .setScale(2, RoundingMode.HALF_UP);

        totalCost = movementQuantity.multiply(unitCost)
                .setScale(2, RoundingMode.HALF_UP);

        } else {

        if (request != null && request.getUnitCost() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unit cost must not be provided for stock OUT"
                );
        }

        // Stock OUT uses the warehouse's weighted-average cost
        // Cost will be calculated after warehouse stock is loaded.
        }


    // Validate supplier
    Supplier supplier = null;

    if (request != null && request.getSupplierId() != null) {
        supplier = supplierRepository
                .findByIdAndFarmId(request.getSupplierId(), farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    // Validate mandatory warehouse
    if (warehouseId == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Warehouse is required for stock movement");
    }

    Warehouse warehouse = warehouseRepository
            .findByIdAndFarmId(warehouseId, farmId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Warehouse not found"));

    // Get or create warehouse-specific stock
    InventoryStock warehouseStock =
            getOrCreateWarehouseStock(item, warehouse);

    BigDecimal currentWarehouseQuantity =
            warehouseStock.getQuantity();

    BigDecimal valuedQuantity =
        warehouseStock.getValuedQuantity() != null
                ? warehouseStock.getValuedQuantity()
                : BigDecimal.ZERO;

BigDecimal currentStockValue =
        warehouseStock.getStockValue() != null
                ? warehouseStock.getStockValue()
                : BigDecimal.ZERO;



    BigDecimal currentQuantity = item.getQuantity();

    BigDecimal updatedWarehouseQuantity;
    BigDecimal updatedQuantity;

    if (type == MovementType.IN) {

        updatedWarehouseQuantity =
                currentWarehouseQuantity.add(movementQuantity);

        BigDecimal newValuedQuantity =
        valuedQuantity.add(movementQuantity);

BigDecimal newStockValue =
        currentStockValue.add(totalCost)
                .setScale(2, RoundingMode.HALF_UP);

BigDecimal newAverageCost =
        newStockValue.divide(
                newValuedQuantity,
                2,
                RoundingMode.HALF_UP
        );

warehouseStock.setValuedQuantity(newValuedQuantity);
warehouseStock.setStockValue(newStockValue);
warehouseStock.setAverageUnitCost(newAverageCost);

        updatedQuantity =
                currentQuantity.add(movementQuantity);


} else {

    // 1. Validate available stock
    if (currentWarehouseQuantity.compareTo(movementQuantity) < 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Insufficient stock in selected warehouse. Available: "
                        + currentWarehouseQuantity);
    }

    if (currentQuantity.compareTo(movementQuantity) < 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Insufficient total farm stock. Available: "
                        + currentQuantity);
    }

    // 2. Calculate updated physical quantity
    updatedWarehouseQuantity =
            currentWarehouseQuantity.subtract(movementQuantity);

    updatedQuantity =
            currentQuantity.subtract(movementQuantity);

    // 3. Separate valued and unvalued stock
    BigDecimal unvaluedQuantity =
            currentWarehouseQuantity.subtract(valuedQuantity)
                    .max(BigDecimal.ZERO);

    // Consume unvalued stock first
    BigDecimal unvaluedConsumed =
            movementQuantity.min(unvaluedQuantity);

    BigDecimal valuedConsumed =
            movementQuantity.subtract(unvaluedConsumed);

unvaluedQuantityConsumed = unvaluedConsumed;
valuedQuantityConsumed = valuedConsumed;

    // 4. Calculate cost only for valued stock consumed
    if (valuedConsumed.compareTo(BigDecimal.ZERO) > 0) {

        BigDecimal averageCost = warehouseStock.getAverageUnitCost();

        if (averageCost == null
                || averageCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Valued stock cost is unavailable.");
        }

        unitCost = averageCost.setScale(2, RoundingMode.HALF_UP);

        totalCost = valuedConsumed.multiply(unitCost)
                .setScale(2, RoundingMode.HALF_UP);

    } else {
        // Cost is unknown for unvalued stock
        unitCost = null;
        totalCost = null;
    }

    // 5. Reduce valued quantity and stock value
    BigDecimal remainingValuedQuantity =
            valuedQuantity.subtract(valuedConsumed);

    BigDecimal remainingStockValue =
            currentStockValue.subtract(
                    totalCost != null ? totalCost : BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

    warehouseStock.setValuedQuantity(remainingValuedQuantity);
    warehouseStock.setStockValue(remainingStockValue);

    // 6. Recalculate weighted average cost
    BigDecimal remainingAverageCost =
            remainingValuedQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? remainingStockValue.divide(
                            remainingValuedQuantity,
                            2,
                            RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

    warehouseStock.setAverageUnitCost(remainingAverageCost);
}


        warehouseStock.setQuantity(updatedWarehouseQuantity);

        inventoryStockRepository.save(warehouseStock);

    // Update farm-level aggregate stock
    item.setQuantity(updatedQuantity);
    inventoryItemRepository.save(item);

    // Link inventory details to crop activity
    if (cropActivity != null) {
        cropActivity.setInventoryItem(item);
        cropActivity.setUsedQuantity(movementQuantity);
    }

    // Save stock movement history
    StockMovement movement = StockMovement.builder()
            .farm(farm)
            .inventoryItem(item)
            .type(type)
            .quantity(movementQuantity)
            .unitCost(unitCost)
            .totalCost(totalCost)
            .valuedQuantityConsumed(valuedQuantityConsumed)
            .unvaluedQuantityConsumed(unvaluedQuantityConsumed)
            .balanceAfter(updatedWarehouseQuantity)
            .supplier(supplier)
            .warehouse(warehouse)
            .cropActivity(cropActivity)
            .remarks(request != null
                    ? request.getRemarks()
                    : "Stock used for crop activity: "
                            + cropActivity.getTitle())
            .build();

    StockMovement savedMovement =
            stockMovementRepository.save(movement);

    return toResponse(savedMovement, updatedQuantity);
}
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getItemHistory(
            Long farmId,
            Long itemId,
            Long userId) {

        verifyFarmOwnership(farmId, userId);

        if (!inventoryItemRepository.findByIdAndFarmId(itemId, farmId)
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Inventory item not found");
        }

        return stockMovementRepository
                .findByFarmIdAndInventoryItemIdOrderByCreatedAtDesc(
                        farmId, itemId)
                .stream()
                .map(movement -> toResponse(
                        movement, movement.getInventoryItem().getQuantity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getFarmHistory(
            Long farmId,
            Long userId) {

        verifyFarmOwnership(farmId, userId);

        return stockMovementRepository
                .findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .map(movement -> toResponse(
                        movement, movement.getInventoryItem().getQuantity()))
                .toList();
    }

    private void verifyFarmOwnership(Long farmId, Long userId) {
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private StockMovementResponse toResponse(
            StockMovement movement,
            BigDecimal availableQuantity) {

        return StockMovementResponse.builder()
                .id(movement.getId())
                .farmId(movement.getFarm().getId())
                .inventoryItemId(movement.getInventoryItem().getId())
                .itemName(movement.getInventoryItem().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .unitCost(movement.getUnitCost())
                .totalCost(movement.getTotalCost())
                .valuedQuantityConsumed(movement.getValuedQuantityConsumed())
                .unvaluedQuantityConsumed(movement.getUnvaluedQuantityConsumed())
                .balanceAfter(movement.getBalanceAfter())
                .availableQuantity(availableQuantity)
                .supplierId(movement.getSupplier() != null
                        ? movement.getSupplier().getId() : null)
                .supplierName(movement.getSupplier() != null
                        ? movement.getSupplier().getName() : null)
                .warehouseId(movement.getWarehouse() != null
                        ? movement.getWarehouse().getId() : null)
                .warehouseName(movement.getWarehouse() != null
                        ? movement.getWarehouse().getName() : null)
                .remarks(movement.getRemarks())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}