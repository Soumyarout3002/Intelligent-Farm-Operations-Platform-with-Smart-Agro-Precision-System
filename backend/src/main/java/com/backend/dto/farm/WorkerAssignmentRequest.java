package com.backend.dto.farm;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAssignmentRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotBlank(message = "Assignment role is required")
    @Size(max = 100)
    private String assignmentRole;

    @Pattern(
        regexp = "ACTIVE|INACTIVE",
        message = "Status must be ACTIVE or INACTIVE"
    )
    private String status;

    @Size(max = 1000)
    private String notes;
}
