
package com.backend.dto.equipment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentBreakdownResponse {

    private Long id;
    private Long equipmentId;
    private String equipment;
    private Long reportedById;
    private String reportedBy;
    private String reason;
    private String description;
    private String status;
    private Long resolvedById;
    private String resolvedBy;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime updatedAt;
}
