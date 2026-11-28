package com.loqma.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String userId,
        String title,
        String text,
        LocalDateTime timestamp,
        Boolean isRead) {
}