
package com.backend.repository;

import com.backend.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository
        extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByFarmId(Long farmId);

    Optional<Warehouse> findByIdAndFarmId(
            Long id,
            Long farmId
    );

    boolean existsByNameIgnoreCaseAndFarmId(
            String name,
            Long farmId
    );
}
