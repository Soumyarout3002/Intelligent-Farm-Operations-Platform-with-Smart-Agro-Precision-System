
package com.backend.dto.equipment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MaintenanceReminderResponse {

    private Long equipmentId;
    private String equipment;
    private String type;
    private LocalDate nextMaintenanceDate;
    private String maintenanceStatus;
    private long daysRemaining;
}
