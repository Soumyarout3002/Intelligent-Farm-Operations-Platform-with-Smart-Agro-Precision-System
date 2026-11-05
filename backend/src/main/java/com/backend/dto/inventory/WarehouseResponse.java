
package com.backend.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WarehouseResponse {

    private Long id;

    private Long farmId;

    private String name;

    private String location;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
