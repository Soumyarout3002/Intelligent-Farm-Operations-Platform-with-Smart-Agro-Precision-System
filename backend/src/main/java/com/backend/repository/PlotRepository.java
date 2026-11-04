package com.backend.repository;

import com.backend.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlotRepository extends JpaRepository<Plot, Long> {

    List<Plot> findByFarmId(Long farmId);

    boolean existsByFarmId(Long farmId);

    Optional<Plot> findByIdAndFarmId(Long plotId, Long farmId);

    boolean existsByFarmIdAndNameIgnoreCase(Long farmId, String name);

    boolean existsByFarmIdAndNameIgnoreCaseAndIdNot(
            Long farmId, String name, Long plotId
    );
}