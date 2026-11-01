package com.backend.repository;

import com.backend.entity.GrowthStageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrowthStageRecordRepository
        extends JpaRepository<GrowthStageRecord, Long> {

    List<GrowthStageRecord> findByCropCycleIdOrderByStartDateAsc(Long cropCycleId);

    Optional<GrowthStageRecord> findByIdAndCropCycleId(
            Long id, Long cropCycleId);
}
