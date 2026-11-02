package com.backend.dto.gis;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotBoundaryRequest {

    @NotEmpty(message = "Boundary coordinates are required")
    private List<List<Double>> coordinates;
}
