
package com.backend.repository;

import com.backend.entity.FarmTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmTaskRepository extends JpaRepository<FarmTask, Long> {

    // Get all tasks of a farm
    List<FarmTask> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    // Find a specific task belonging to a farm
    Optional<FarmTask> findByIdAndFarmId(Long taskId, Long farmId);

    // Get tasks assigned to a particular worker
    List<FarmTask> findByAssignedWorker_IdOrderByCreatedAtDesc(
        Long assignmentId
    );

    // Count tasks by status for a farm
    long countByFarmIdAndStatus(Long farmId, String status);
}
