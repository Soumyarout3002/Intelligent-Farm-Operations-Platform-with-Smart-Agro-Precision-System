package com.backend.dto.irrigation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IrrigationScheduleRequest {

    @NotNull(message = "Scheduled date and time are required")
    @Future(message = "Scheduled date and time must be in the future")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Water amount is required")
    @Positive(message = "Water amount must be greater than zero")
    private Double waterAmountLiters;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}