package com.backend.repository;

import com.backend.entity.CropCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropCycleRepository extends JpaRepository<CropCycle, Long> {

    List<CropCycle> findByPlotIdOrderByCreatedAtDesc(Long plotId);

    Optional<CropCycle> findByIdAndPlotId(Long id, Long plotId);
}
