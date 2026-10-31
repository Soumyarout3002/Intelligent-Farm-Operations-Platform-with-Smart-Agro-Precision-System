package com.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotRequest {

    @NotBlank(message = "Plot name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String plotCode;

    @NotNull(message = "Plot area is required")
    @DecimalMin(value = "0.01", message = "Area must be greater than zero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal area;

    @NotBlank(message = "Area unit is required")
    @Pattern(
        regexp = "ACRE|HECTARE|SQ_METER",
        message = "Use ACRE, HECTARE or SQ_METER"
    )
    private String areaUnit;

    @Size(max = 500)
    private String description;
}
