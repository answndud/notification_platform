package com.example.notification.domain.metrics.controller;

import com.example.notification.domain.metrics.dto.NotificationMetricsResponse;
import com.example.notification.domain.metrics.service.NotificationMetricsService;
import com.example.notification.global.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/metrics")
public class NotificationMetricsController {

    private final NotificationMetricsService notificationMetricsService;

    public NotificationMetricsController(NotificationMetricsService notificationMetricsService) {
        this.notificationMetricsService = notificationMetricsService;
    }

    @GetMapping
    public ApiResponse<NotificationMetricsResponse> getMetrics() {
        return ApiResponse.ok(notificationMetricsService.getMetrics());
    }
}
