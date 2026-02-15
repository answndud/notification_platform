package com.example.notification.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record NotificationRequestCreateRequest(
        @NotBlank String requestKey,
        @NotBlank String templateCode,
        @NotEmpty List<Long> receiverIds,
        Map<String, Object> variables,
        @NotBlank String priority
) {
}
