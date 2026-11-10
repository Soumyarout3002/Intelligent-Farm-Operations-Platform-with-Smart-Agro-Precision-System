package com.backend.dto.equipment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentBookingResponse {

    private Long id;

    private Long equipmentId;
    private String equipment;

    private Long farmerId;
    private String farmer;

    private LocalDate date;
    private String status;

    private Long reviewedById;
    private String reviewedBy;

    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}