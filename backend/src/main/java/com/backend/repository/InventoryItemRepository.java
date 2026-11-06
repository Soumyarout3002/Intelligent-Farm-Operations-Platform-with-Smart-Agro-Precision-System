
package com.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.entity.InventoryItem;

import jakarta.persistence.LockModeType;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByFarmId(Long farmId);

    Optional<InventoryItem> findByIdAndFarmId(
            Long id,
            Long farmId
    );

    boolean existsByNameIgnoreCaseAndFarmId(
            String name,
            Long farmId
    );

    // Find items whose current stock is at or below minimum stock.
    @Query("""
            SELECT i FROM InventoryItem i
            WHERE i.farm.id = :farmId
            AND i.quantity <= i.minimum
            ORDER BY i.quantity ASC
            """)
    List<InventoryItem> findLowStockItems(
            @Param("farmId") Long farmId
    );

    // Find items expiring on or before the given date.
    // Already expired items are also included.
    @Query("""
            SELECT i FROM InventoryItem i
            WHERE i.farm.id = :farmId
            AND i.expiryDate IS NOT NULL
            AND i.expiryDate <= :expiryLimit
            ORDER BY i.expiryDate ASC
            """)
    List<InventoryItem> findItemsExpiringBefore(
            @Param("farmId") Long farmId,
            @Param("expiryLimit") LocalDate expiryLimit
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :itemId AND i.farm.id = :farmId")
    Optional<InventoryItem> findByIdAndFarmIdForUpdate(
            @Param("itemId") Long itemId,
            @Param("farmId") Long farmId
    );
}
