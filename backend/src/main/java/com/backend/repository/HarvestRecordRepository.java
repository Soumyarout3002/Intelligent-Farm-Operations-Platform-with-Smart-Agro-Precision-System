package com.backend.repository;

import com.backend.entity.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HarvestRecordRepository
        extends JpaRepository<HarvestRecord, Long> {

    List<HarvestRecord> findByCropCycleIdOrderByHarvestDateDesc(
            Long cropCycleId);

    Optional<HarvestRecord> findByIdAndCropCycleId(
            Long id, Long cropCycleId);
}