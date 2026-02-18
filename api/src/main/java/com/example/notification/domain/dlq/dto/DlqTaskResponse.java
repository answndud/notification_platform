package com.example.notification.domain.dlq.dto;

import java.time.LocalDateTime;

public record DlqTaskResponse(
        Long taskId,
        Long requestId,
        Long receiverId,
        String channel,
        String priority,
        String status,
        int retryCount,
        int maxRetry,
        LocalDateTime nextRetryAt,
        String lastResultCode,
        String lastResultMessage,
        LocalDateTime createdAt
) {
}
