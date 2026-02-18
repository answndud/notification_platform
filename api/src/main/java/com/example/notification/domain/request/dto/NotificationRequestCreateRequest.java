package com.example.notification.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record NotificationRequestCreateRequest(
        @NotBlank @Size(max = 120) String requestKey,
        @NotBlank @Size(max = 80) String templateCode,
        @NotEmpty @Size(max = 1000) List<@Positive Long> receiverIds,
        Map<String, Object> variables,
        @NotBlank @Pattern(regexp = "(?i)HIGH|NORMAL|LOW") String priority
) {
}
