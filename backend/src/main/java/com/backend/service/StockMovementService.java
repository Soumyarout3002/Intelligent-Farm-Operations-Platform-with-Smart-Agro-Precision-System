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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final FarmRepository farmRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    public StockMovementResponse stockIn(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
        farmId, itemId, userId, request, MovementType.IN, null, null);
    }

    public StockMovementResponse stockOut(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
        farmId, itemId, userId, request, MovementType.OUT, null, null);
    }

    public StockMovementResponse stockOutForCropUsage(
        Long farmId,
        Long itemId,
        Long userId,
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
            usedQuantity);
}
    public boolean hasMovementForCropActivity(Long cropActivityId) {
    return stockMovementRepository
            .existsByCropActivityId(cropActivityId);
}

    private StockMovementResponse processMovement(
        Long farmId,
        Long itemId,
        Long userId,
        StockMovementRequest request,
        MovementType type,
        CropActivity cropActivity,
        BigDecimal overrideQuantity) {

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

        // Validate optional supplier
        Supplier supplier = null;

        if (request != null && request.getSupplierId() != null) {
            supplier = supplierRepository
                    .findByIdAndFarmId(request.getSupplierId(), farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Supplier not found"));
        }

        // Validate optional warehouse
        Warehouse warehouse = null;

        if (request != null && request.getWarehouseId() != null) {            warehouse = warehouseRepository
                    .findByIdAndFarmId(request.getWarehouseId(), farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Warehouse not found"));
        }

        BigDecimal currentQuantity = item.getQuantity();
        BigDecimal updatedQuantity;

        if (type == MovementType.IN) {
            updatedQuantity = currentQuantity.add(movementQuantity);
        } else {
            if (currentQuantity.compareTo(movementQuantity) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock. Available quantity: "
                                + currentQuantity);
            }

            updatedQuantity = currentQuantity.subtract(movementQuantity);
        }

        // Update inventory quantity
        item.setQuantity(updatedQuantity);
        inventoryItemRepository.save(item);
        // Link inventory item and quantity to crop activity
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