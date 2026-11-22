
package com.backend.service;

import com.backend.dto.notification.NotificationResponse;
import com.backend.dto.notification.UnreadCountResponse;
import com.backend.entity.Notification;
import com.backend.entity.User;
import com.backend.repository.NotificationRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(
            Long userId,
            Long farmId,
            String title,
            String message,
            String type,
            String dedupeKey) {

        // Dedupe independently for each recipient. A weather alert already sent
        // to an Admin must not suppress the same alert for an assigned Field Worker.
        String recipientDedupeKey = userId + ":" + dedupeKey;
        if (notificationRepository.existsByDedupeKey(recipientDedupeKey)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification user not found"));

        Notification notification = Notification.builder()
                .user(user)
                .farmId(farmId)
                .title(title)
                .message(message)
                .type(type)
                .dedupeKey(recipientDedupeKey)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        return new UnreadCountResponse(
                notificationRepository.countByUser_IdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = getUserNotification(id, userId);
        notification.setRead(true);
        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications =
                notificationRepository.findByUser_IdAndReadFalse(userId);

        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(Long id, Long userId) {
        notificationRepository.delete(getUserNotification(id, userId));
    }

    private Notification getUserNotification(Long id, Long userId) {
        return notificationRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .farmId(n.getFarmId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}