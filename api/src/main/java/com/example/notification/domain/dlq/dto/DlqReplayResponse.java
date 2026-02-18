package com.example.notification.domain.dlq.dto;

import java.time.LocalDateTime;

public record DlqReplayResponse(
        Long taskId,
        String status,
        int retryCount,
        LocalDateTime nextRetryAt
) {
}
