
package com.backend.controller;

import com.backend.dto.equipment.MaintenanceReminderResponse;
import com.backend.entity.User;
import com.backend.service.MaintenanceReminderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/equipment/maintenance")
@RequiredArgsConstructor
public class MaintenanceReminderController {

    private final MaintenanceReminderService maintenanceService;

    @GetMapping("/reminders")
    public ResponseEntity<List<MaintenanceReminderResponse>> getReminders(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                maintenanceService.getMaintenanceReminders(
                        farmId, user.getId())
        );
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        return user;
    }
}
