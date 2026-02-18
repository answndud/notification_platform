package com.example.notification.domain.dlq.dto;

import java.util.List;

public record DlqListResponse(
        List<DlqTaskResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
