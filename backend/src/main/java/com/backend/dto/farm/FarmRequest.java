package com.backend.dto.farm;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmRequest {

    @NotBlank(message = "Farm name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    private String location;

    @NotNull(message = "Farm area is required")
    @DecimalMin(value = "0.01", message = "Area must be greater than zero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal area;

    @NotBlank(message = "Area unit is required")
    @Pattern(regexp = "ACRE|HECTARE|SQ_METER",
            message = "Use ACRE, HECTARE or SQ_METER")
    private String areaUnit;

    @Size(max = 50)
    private String soilType;

    @Size(max = 100)
    private String primaryCrop;

    private String notes;
}
