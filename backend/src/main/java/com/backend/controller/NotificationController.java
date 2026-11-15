
package com.backend.controller;

import com.backend.dto.notification.NotificationResponse;
import com.backend.dto.notification.UnreadCountResponse;
import com.backend.entity.User;
import com.backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            HttpServletRequest request) {
        return notificationService.getNotifications(getUserId(request));
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(
            HttpServletRequest request) {
        return notificationService.getUnreadCount(getUserId(request));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        return notificationService.markAsRead(id, getUserId(request));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(
            HttpServletRequest request) {
        notificationService.markAllAsRead(getUserId(request));
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        notificationService.deleteNotification(id, getUserId(request));
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return user.getId();
    }
}