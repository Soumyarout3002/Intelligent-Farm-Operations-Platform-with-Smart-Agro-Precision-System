package com.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAssignmentResponse {

    private Long id;

    private Long farmId;
    private String farmName;

    private Long workerId;
    private String workerName;
    private String workerEmail;
    private String workerMobile;

    private String assignmentRole;
    private String status;
    private String notes;

    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
