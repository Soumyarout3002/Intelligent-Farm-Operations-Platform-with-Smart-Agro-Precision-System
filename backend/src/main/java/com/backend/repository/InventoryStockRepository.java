package com.backend.repository;

import com.backend.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByInventoryItemIdAndWarehouseId(
            Long inventoryItemId,
            Long warehouseId
    );

    List<InventoryStock> findByInventoryItemId(Long inventoryItemId);
    boolean existsByInventoryItemId(Long inventoryItemId);

    List<InventoryStock> findByWarehouseId(Long warehouseId);

    boolean existsByWarehouseIdAndQuantityGreaterThan(
            Long warehouseId,
            java.math.BigDecimal quantity
    );

boolean existsByWarehouse_Id(Long warehouseId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM InventoryStock s
        WHERE s.inventoryItem.id = :itemId
        AND s.warehouse.id = :warehouseId
        """)
    Optional<InventoryStock> findByItemAndWarehouseForUpdate(
            @Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId
    );
}