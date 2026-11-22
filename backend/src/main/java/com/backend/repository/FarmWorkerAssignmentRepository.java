
package com.backend.repository;

import com.backend.entity.FarmWorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmWorkerAssignmentRepository
        extends JpaRepository<FarmWorkerAssignment, Long> {

    List<FarmWorkerAssignment> findByFarmId(Long farmId);

    Optional<FarmWorkerAssignment> findByIdAndFarmId(
            Long id, Long farmId);

    boolean existsByFarmIdAndWorkerId(Long farmId, Long workerId);

    List<FarmWorkerAssignment> findByWorker_IdAndStatusIgnoreCase(
            Long workerId, String status);

            boolean existsByFarmIdAndWorkerIdAndStatusIgnoreCase(
        Long farmId, Long workerId, String status);
}
