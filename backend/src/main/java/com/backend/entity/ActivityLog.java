
package com.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "activity_logs",
    indexes = {
        @Index(name = "idx_activity_actor", columnList = "actor_user_id"),
        @Index(name = "idx_activity_action", columnList = "action_type"),
        @Index(name = "idx_activity_created", columnList = "created_at")
    }
)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who performed the action.
    // No foreign key: history must survive user deletion.
    @Column(name = "actor_user_id")
    private Long actorUserId;

    // Preserve actor information even if the user is deleted.
    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Column(name = "actor_role", length = 30)
    private String actorRole;

    // Example: USER_CREATED, PROFILE_UPDATED, LOGIN_SUCCESS.
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    // Example: USER, PROFILE, AUTH_SESSION.
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    // ID of the affected record.
    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(length = 500)
    private String description;

    // Store only safe, relevant before/after values in JSON format.
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ActivityLog() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
