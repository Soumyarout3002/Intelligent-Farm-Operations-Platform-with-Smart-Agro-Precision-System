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
public class CropActivityRequest {

    @NotBlank(message = "Activity type is required")
    @Size(max = 50)
    private String activityType;

    @NotBlank(message = "Activity title is required")
    @Size(max = 150)
    private String title;

    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    private String description;

    private String quantity;

    private String performedBy;
}