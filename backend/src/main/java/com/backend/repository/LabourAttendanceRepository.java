
package com.backend.repository;

import com.backend.entity.LabourAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LabourAttendanceRepository
        extends JpaRepository<LabourAttendance, Long> {

    // Check duplicate attendance for a worker on a date
    boolean existsByAssignmentIdAndAttendanceDate(
            Long assignmentId,
            LocalDate attendanceDate
    );

    // Get attendance records for a farm on a particular date
    List<LabourAttendance> findByAssignmentFarmIdAndAttendanceDate(
            Long farmId,
            LocalDate attendanceDate
    );

    // Get all attendance records for a farm
    List<LabourAttendance> findByAssignmentFarmIdOrderByAttendanceDateDesc(
            Long farmId
    );

    // Find a specific attendance record
    Optional<LabourAttendance> findByIdAndAssignmentFarmId(
            Long id,
            Long farmId
    );

    // Get attendance records for a specific crop cycle
    List<LabourAttendance> findByCropCycleId(Long cropCycleId);

    @Query("""
        SELECT COALESCE(SUM(a.dailyWage), 0)
        FROM LabourAttendance a
        WHERE a.cropCycle.id = :cropCycleId
        AND UPPER(a.status) = UPPER(:status)
        """)
    BigDecimal sumDailyWageByCropCycleIdAndStatus(
            @Param("cropCycleId") Long cropCycleId,
            @Param("status") String status
    );
}
