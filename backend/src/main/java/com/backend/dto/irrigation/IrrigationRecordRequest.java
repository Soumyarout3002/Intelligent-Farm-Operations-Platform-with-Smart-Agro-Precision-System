package com.backend.dto.irrigation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IrrigationRecordRequest {

    @NotBlank(message = "Action is required")
    @Pattern(
            regexp = "COMPLETED|SKIPPED",
            message = "Action must be COMPLETED or SKIPPED"
    )
    private String action;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}