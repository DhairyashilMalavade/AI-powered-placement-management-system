package com.dhairya.Placement_management_system.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private boolean isRead;
    private String linkUrl;
    private LocalDateTime createdAt;
}
