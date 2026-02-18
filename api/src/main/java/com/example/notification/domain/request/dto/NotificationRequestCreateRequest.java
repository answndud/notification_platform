package com.example.notification.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

public record NotificationRequestCreateRequest(
        @NotBlank String requestKey,
        @NotBlank String templateCode,
        @NotEmpty List<@Positive Long> receiverIds,
        Map<String, Object> variables,
        @NotBlank @Pattern(regexp = "(?i)HIGH|NORMAL|LOW") String priority
) {
}
