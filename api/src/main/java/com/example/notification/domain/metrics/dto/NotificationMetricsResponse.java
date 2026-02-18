package com.example.notification.domain.metrics.dto;

public record NotificationMetricsResponse(
        long pendingTasks,
        long sendingTasks,
        long sentTasks,
        long failedTasks,
        long dlqTasks,
        long requestQueuedLag,
        long malformedQueuedLag,
        double successRate,
        double averageLatencyMs
) {
}
