package com.example.notification.domain.request.producer;

import java.util.List;
import java.util.Map;

public record NotificationRequestQueuedEvent(
        Long requestId,
        String requestKey,
        String templateCode,
        String priority,
        List<Long> receiverIds,
        Map<String, Object> variables
) {
}
