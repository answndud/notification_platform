package com.example.notification.domain.request.dto;

import com.example.notification.domain.request.entity.NotificationRequest;
import java.time.LocalDateTime;

public record NotificationRequestResponse(
        Long requestId,
        String requestKey,
        String status,
        LocalDateTime requestedAt
) {
    public static NotificationRequestResponse from(NotificationRequest request) {
        return new NotificationRequestResponse(
                request.getId(),
                request.getRequestKey(),
                request.getStatus().name(),
                request.getRequestedAt()
        );
    }
}
