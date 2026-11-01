package com.backend.repository;

import com.backend.entity.PestDiseaseObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PestDiseaseObservationRepository
        extends JpaRepository<PestDiseaseObservation, Long> {

    List<PestDiseaseObservation>
    findByCropCycleIdOrderByObservationDateDesc(Long cropCycleId);

    Optional<PestDiseaseObservation> findByIdAndCropCycleId(
            Long id, Long cropCycleId);
}
