package com.backend.repository;

import com.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import com.backend.entity.StockMovement.MovementType;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByFarmIdAndInventoryItemIdOrderByCreatedAtDesc(
            Long farmId,
            Long inventoryItemId
    );

    List<StockMovement> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    // Check whether stock was already deducted for a crop activity

    boolean existsByCropActivityId(Long cropActivityId);
    boolean existsByInventoryItemId(Long inventoryItemId);

    boolean existsByWarehouse_Id(Long warehouseId);

    @Query("""
                SELECT COALESCE(SUM(sm.quantity), 0)
                FROM StockMovement sm
                WHERE sm.inventoryItem.id = :itemId
                AND sm.type = com.backend.entity.StockMovement.MovementType.IN
                """)
        BigDecimal getTotalPurchasedQuantity(
                @Param("itemId") Long itemId
        );

        @Query("""
                SELECT COALESCE(SUM(sm.totalCost), 0)
                FROM StockMovement sm
                WHERE sm.inventoryItem.id = :itemId
                AND sm.type = com.backend.entity.StockMovement.MovementType.IN
                """)
        BigDecimal getTotalPurchaseCost(
                @Param("itemId") Long itemId
        );

        @Query("""
                SELECT COALESCE(SUM(sm.quantity), 0)
                FROM StockMovement sm
                WHERE sm.inventoryItem.id = :itemId
                AND sm.type = com.backend.entity.StockMovement.MovementType.OUT
                """)
        BigDecimal getTotalConsumedQuantity(
                @Param("itemId") Long itemId
        );

        @Query("""
                SELECT COALESCE(SUM(sm.totalCost), 0)
                FROM StockMovement sm
                WHERE sm.inventoryItem.id = :itemId
                AND sm.type = com.backend.entity.StockMovement.MovementType.OUT
                """)
        BigDecimal getTotalConsumedCost(
                @Param("itemId") Long itemId
        );
        boolean existsByInventoryItemIdAndWarehouseId(
        Long inventoryItemId,
        Long warehouseId
);
        @Query("""
        SELECT COALESCE(SUM(sm.totalCost), 0)
        FROM StockMovement sm
        WHERE sm.cropActivity.cropCycle.id = :cropCycleId
        AND sm.type = :movementType
        AND LOWER(sm.inventoryItem.category) = LOWER(:category)
        AND sm.totalCost IS NOT NULL
        """)
        BigDecimal sumConsumedCostByCropCycleAndCategory(
                @Param("cropCycleId") Long cropCycleId,
                @Param("movementType") MovementType movementType,
                @Param("category") String category
        );
}