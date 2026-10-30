package com.backend.controller;

import com.backend.entity.ActivityLog;
import com.backend.repository.ActivityLogRepository;
import com.backend.repository.ActivityLogSpecification;
import com.backend.security.RequireRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequireRole("ADMIN")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogController(
            ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    // GET /api/admin/activity-logs
    @GetMapping
    public ResponseEntity<?> getActivityLogs(

            @RequestParam(required = false) Long userId,

            @RequestParam(required = false) String actionType,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Invalid pagination values."));
        }

        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Start date cannot be after end date."));
        }

        LocalDateTime startDateTime = startDate == null
                ? null : startDate.atStartOfDay();

        LocalDateTime endDateTime = endDate == null
                ? null : endDate.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ActivityLog> result = activityLogRepository.findAll(
                ActivityLogSpecification.filter(
                        userId,
                        actionType,
                        startDateTime,
                        endDateTime),
                pageable);

        return ResponseEntity.ok(toPageResponse(result));
    }

    // GET /api/admin/activity-logs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityLog(@PathVariable Long id) {

        return activityLogRepository.findById(id)
                .<ResponseEntity<?>>map(log ->
                        ResponseEntity.ok(toLogResponse(log)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message",
                                "Activity log not found.")));
    }

    private Map<String, Object> toPageResponse(Page<ActivityLog> page) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("content",
                page.getContent().stream()
                        .map(this::toLogResponse)
                        .toList());

        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("first", page.isFirst());
        response.put("last", page.isLast());

        return response;
    }

    private Map<String, Object> toLogResponse(ActivityLog log) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", log.getId());
        response.put("actorUserId", log.getActorUserId());
        response.put("actorName", log.getActorName());
        response.put("actorRole", log.getActorRole());
        response.put("actionType", log.getActionType());
        response.put("entityType", log.getEntityType());
        response.put("entityId", log.getEntityId());
        response.put("description", log.getDescription());
        response.put("oldValues", log.getOldValues());
        response.put("newValues", log.getNewValues());
        response.put("ipAddress", log.getIpAddress());
        response.put("createdAt", log.getCreatedAt());

        return response;
    }
}