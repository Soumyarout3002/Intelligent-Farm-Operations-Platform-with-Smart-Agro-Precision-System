
package com.backend.dto.labour;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LabourAttendanceResponse {

    private Long id;

    private Long assignmentId;

    private Long cropCycleId;

    private Long workerId;

    private String workerName;

    private String attendanceDate;

    private String status;

    private BigDecimal dailyWage;

    private String notes;

    private LocalDateTime createdAt;
}
