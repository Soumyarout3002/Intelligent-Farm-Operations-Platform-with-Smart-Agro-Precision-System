
package com.backend.dto.task;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class FarmTaskResponse {

    private Long id;

    private Long farmId;

    private String title;

    private String description;

    private Long assignmentId;

    private Long workerId;

    private String workerName;

    private LocalDate dueDate;

    private String priority;

    private String status;

    private Long createdById;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
