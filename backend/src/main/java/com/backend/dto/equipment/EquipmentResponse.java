package com.backend.dto.equipment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentResponse {

    private Long id;
    private Long farmId;
    private String farm;
    private String name;
    private String type;
    private String status;
    private String model;
    private String power;
    private String fuel;
    private Integer year;
    private String condition;
    private LocalDate maintenance;
    private String description;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}