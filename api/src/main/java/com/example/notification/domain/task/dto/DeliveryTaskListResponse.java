package com.example.notification.domain.task.dto;

import java.util.List;

public record DeliveryTaskListResponse(
        List<DeliveryTaskResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
