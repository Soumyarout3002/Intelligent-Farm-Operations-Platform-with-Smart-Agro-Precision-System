package com.backend.repository;

import com.backend.entity.CropActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropActivityRepository
        extends JpaRepository<CropActivity, Long> {

    List<CropActivity> findByCropCycleIdOrderByActivityDateDesc(
            Long cropCycleId);

    Optional<CropActivity> findByIdAndCropCycleId(
            Long id, Long cropCycleId);
}
