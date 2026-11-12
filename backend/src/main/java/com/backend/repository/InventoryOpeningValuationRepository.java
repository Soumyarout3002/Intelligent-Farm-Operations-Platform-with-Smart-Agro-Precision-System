
package com.backend.repository;

import com.backend.entity.InventoryOpeningValuation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryOpeningValuationRepository
        extends JpaRepository<InventoryOpeningValuation, Long> {

    Optional<InventoryOpeningValuation>
    findByInventoryItemIdAndWarehouseId(
            Long inventoryItemId,
            Long warehouseId
    );

    List<InventoryOpeningValuation> findByInventoryItemId(
            Long inventoryItemId
    );

    boolean existsByInventoryItemIdAndWarehouseId(
            Long inventoryItemId,
            Long warehouseId
    );
}
