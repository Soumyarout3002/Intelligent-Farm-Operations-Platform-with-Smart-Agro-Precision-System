
package com.backend.service;

import com.backend.entity.ActivityLog;
import com.backend.entity.User;
import com.backend.repository.ActivityLogRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            ObjectMapper objectMapper) {

        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Records an activity performed by an authenticated user.
     *
     * Sensitive information such as passwords, password hashes,
     * bearer tokens and reset tokens must never be passed here.
     */
    @Transactional
    public void logActivity(
            User actor,
            String actionType,
            String entityType,
            String entityId,
            String description,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            HttpServletRequest request) {

        ActivityLog activity = new ActivityLog();

        // Capture actor information before the activity is saved.
        if (actor != null) {
            activity.setActorUserId(actor.getId());
            activity.setActorName(actor.getName());
            activity.setActorRole(actor.getRole());
        }

        activity.setActionType(actionType);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setDescription(description);

        // Convert before/after values into JSON strings.
        activity.setOldValues(toJson(oldValues));
        activity.setNewValues(toJson(newValues));

        // Capture the direct client address.
        // Do not blindly trust X-Forwarded-For headers.
        if (request != null) {
            activity.setIpAddress(request.getRemoteAddr());
        }

        activityLogRepository.save(activity);
    }

    private String toJson(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(values);
        } catch (JacksonException ex) {
            throw new IllegalStateException(
                    "Unable to serialize activity details.", ex);
        }
    }
}
