package com.backend.repository;

import com.backend.entity.SoilRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoilRecordRepository
        extends JpaRepository<SoilRecord, Long> {

    List<SoilRecord> findByPlotIdOrderByRecordedAtDesc(Long plotId);
    boolean existsByPlotId(Long plotId);

    Optional<SoilRecord> findByIdAndPlotId(Long id, Long plotId);
}
