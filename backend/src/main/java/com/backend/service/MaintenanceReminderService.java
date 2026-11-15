
package com.backend.service;

import com.backend.dto.equipment.MaintenanceReminderResponse;
import com.backend.entity.Equipment;
import com.backend.repository.EquipmentRepository;
import com.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceReminderService {

    private final EquipmentRepository equipmentRepository;
    private final FarmRepository farmRepository;
    private final NotificationService notificationService;

    public List<MaintenanceReminderResponse> getMaintenanceReminders(
        Long farmId, Long ownerId) {

    farmRepository.findByIdAndOwnerId(farmId, ownerId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Farm not found or access denied"));

    LocalDate today = LocalDate.now();
    LocalDate upcomingLimit = today.plusDays(30);

    List<MaintenanceReminderResponse> reminders =
            equipmentRepository.findByFarmId(farmId)
                    .stream()
                    .filter(equipment ->
                            equipment.getNextMaintenanceDate() != null
                            && !equipment.getNextMaintenanceDate()
                                    .isAfter(upcomingLimit))
                    .map(equipment -> toResponse(equipment, today))
                    .toList();

    for (MaintenanceReminderResponse reminder : reminders) {

        notificationService.createNotification(
                ownerId,
                farmId,
                "Equipment maintenance reminder",
                reminder.getEquipment() + " maintenance status: "
                        + reminder.getMaintenanceStatus()
                        + ". Maintenance date: "
                        + reminder.getNextMaintenanceDate(),
                "EQUIPMENT",
                "MAINTENANCE:" + reminder.getEquipmentId()
                        + ":" + reminder.getNextMaintenanceDate()
        );
    }

    return reminders;
}

    private MaintenanceReminderResponse toResponse(
            Equipment equipment, LocalDate today) {

        LocalDate maintenanceDate = equipment.getNextMaintenanceDate();
        long daysRemaining = ChronoUnit.DAYS.between(
                today, maintenanceDate);

        String status;

        if (daysRemaining < 0) {
            status = "OVERDUE";
        } else if (daysRemaining == 0) {
            status = "DUE_TODAY";
        } else {
            status = "UPCOMING";
        }

        return MaintenanceReminderResponse.builder()
                .equipmentId(equipment.getId())
                .equipment(equipment.getName())
                .type(equipment.getType())
                .nextMaintenanceDate(maintenanceDate)
                .maintenanceStatus(status)
                .daysRemaining(daysRemaining)
                .build();
    }
}
