package com.backend.repository;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :itemId AND i.farm.id = :farmId")
    Optional<InventoryItem> findByIdAndFarmIdForUpdate(
            @Param("itemId") Long itemId,
            @Param("farmId") Long farmId
    );

    
}