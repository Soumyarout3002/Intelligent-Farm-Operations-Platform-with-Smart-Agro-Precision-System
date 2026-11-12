
package com.backend.dto.labour;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LabourAttendanceRequest {

    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotBlank(message = "Attendance status is required")
    @Pattern(
        regexp = "PRESENT|ABSENT",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Status must be PRESENT or ABSENT"
    )
    private String status;

    @DecimalMin(
        value = "0.0",
        message = "Daily wage cannot be negative"
    )
    private BigDecimal dailyWage;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Positive(message = "Crop cycle ID must be positive")
    private Long cropCycleId;
}
