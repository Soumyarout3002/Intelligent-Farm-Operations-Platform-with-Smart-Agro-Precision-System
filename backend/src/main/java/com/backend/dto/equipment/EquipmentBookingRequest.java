package com.backend.dto.equipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EquipmentBookingRequest {

    @NotNull
    @Positive
    private Long equipmentId;

    @NotNull
    private LocalDate bookingDate;
}