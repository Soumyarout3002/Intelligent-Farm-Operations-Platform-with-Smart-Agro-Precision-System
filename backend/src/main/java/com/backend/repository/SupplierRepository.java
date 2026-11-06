
package com.backend.repository;

import com.backend.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository
        extends JpaRepository<Supplier, Long> {

    List<Supplier> findByFarmId(Long farmId);

    Optional<Supplier> findByIdAndFarmId(
            Long id,
            Long farmId
    );

    boolean existsByNameIgnoreCaseAndFarmId(
            String name,
            Long farmId
    );
}
