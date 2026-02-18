package com.example.notification.domain.task.dto;

import java.time.LocalDateTime;

public record DeliveryTaskResponse(
        Long taskId,
        Long requestId,
        Long receiverId,
        String channel,
        String priority,
        String status,
        int retryCount,
        int maxRetry,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt
) {
}
