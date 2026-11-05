
package com.backend.repository;

import com.backend.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
}
