package com.dhairya.Placement_management_system.notification;

import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.notification.dto.NotificationResponse;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponse create(UUID userId, String title, String message) {
        return create(userId, title, message, null);
    }

    @Transactional
    public NotificationResponse create(UUID userId, String title, String message, String linkUrl) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLinkUrl(linkUrl);
        notification = notificationRepository.save(notification);

        return toResponse(notification);
    }

    public PagedResponse<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toResponse);
        return PagedResponse.from(page);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException("Notification does not belong to this user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository
            .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUser().getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.isRead(),
            notification.getLinkUrl(),
            notification.getCreatedAt());
    }
}
