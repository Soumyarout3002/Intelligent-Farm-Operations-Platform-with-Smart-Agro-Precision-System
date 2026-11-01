package com.backend.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CropCycleRequest {

    @NotBlank(message = "Crop name is required")
    @Size(max = 100)
    private String cropName;

    @Size(max = 100)
    private String variety;

    @Size(max = 100)
    private String season;

    @NotNull(message = "Sowing date is required")
    private LocalDate sowingDate;

    private LocalDate expectedHarvestDate;

    private String status;

    private String notes;
}