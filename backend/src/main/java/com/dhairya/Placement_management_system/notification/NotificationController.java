package com.dhairya.Placement_management_system.notification;

import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.notification.dto.NotificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponse>> getMy(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Long>> getUnreadCount(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        notificationService.markAsRead(id, userId);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        notificationService.markAllAsRead(userId);
    }
}
