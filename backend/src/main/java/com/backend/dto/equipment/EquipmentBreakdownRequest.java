
package com.backend.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentBreakdownRequest {

    @NotNull
    @Positive
    private Long equipmentId;

    @NotBlank
    @Size(max = 100)
    private String reason;

    @NotBlank
    @Size(max = 2000)
    private String description;
}
