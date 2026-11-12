package com.backend.dto.equipment;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EquipmentRequest {

    @NotNull(message = "Farm ID is required")
    @Positive(message = "Farm ID must be positive")
    private Long farmId;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 150, message = "Equipment name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Equipment type is required")
    @Size(max = 50)
    private String type;

    @Size(max = 100)
    private String model;

    @Size(max = 50)
    private String power;

    @Size(max = 30)
    private String fuelType;

    @Min(value = 1950, message = "Manufacturing year must be 1950 or later")
    @Max(value = 2100, message = "Manufacturing year is invalid")
    private Integer manufacturingYear;

    @Size(max = 30)
    private String condition;

    private LocalDate nextMaintenanceDate;

    @Size(max = 1000)
    private String description;

    @Size(max = 1000)
    private String imageUrl;

    // Status is intentionally excluded.
    // The backend controls equipment status.
}