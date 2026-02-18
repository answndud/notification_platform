package com.example.notification.domain.request.dto;

import java.util.List;

public record NotificationRequestListResponse(
        List<NotificationRequestResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
