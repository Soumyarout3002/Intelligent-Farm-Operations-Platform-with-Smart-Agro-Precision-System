
package com.backend.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FarmTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @NotNull(message = "Worker assignment ID is required")
    private Long assignmentId;

    private LocalDate dueDate;

    @NotBlank(message = "Priority is required")
    @Pattern(
        regexp = "LOW|MEDIUM|HIGH",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Priority must be LOW, MEDIUM or HIGH"
    )
    private String priority;
}
