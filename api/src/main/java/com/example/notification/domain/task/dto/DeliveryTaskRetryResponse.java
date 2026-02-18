package com.example.notification.domain.task.dto;

import java.time.LocalDateTime;

public record DeliveryTaskRetryResponse(
        Long taskId,
        String status,
        int retryCount,
        LocalDateTime nextRetryAt
) {
}
