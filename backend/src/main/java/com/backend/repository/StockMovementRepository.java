package com.backend.repository;

import com.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByFarmIdAndInventoryItemIdOrderByCreatedAtDesc(
            Long farmId,
            Long inventoryItemId
    );

    List<StockMovement> findByFarmIdOrderByCreatedAtDesc(Long farmId);
}