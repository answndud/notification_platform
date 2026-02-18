package com.example.notification.worker.service;

import com.example.notification.worker.consumer.NonRetryableEventException;
import com.example.notification.worker.consumer.NonRetryableReasonCode;
import com.example.notification.worker.domain.log.entity.DeliveryLog;
import com.example.notification.worker.domain.log.repository.DeliveryLogRepository;
import com.example.notification.worker.domain.policy.entity.ChannelPolicy;
import com.example.notification.worker.domain.policy.repository.ChannelPolicyRepository;
import com.example.notification.worker.domain.task.entity.DeliveryTask;
import com.example.notification.worker.domain.task.entity.DeliveryTaskStatus;
import com.example.notification.worker.domain.task.repository.DeliveryTaskRepository;
import com.example.notification.worker.sender.MockChannelSender;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);
    private static final int RETRY_BATCH_SIZE = 50;

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final ChannelPolicyRepository channelPolicyRepository;
    private final MockChannelSender mockChannelSender;

    public NotificationDeliveryService(
            DeliveryTaskRepository deliveryTaskRepository,
            DeliveryLogRepository deliveryLogRepository,
            ChannelPolicyRepository channelPolicyRepository,
            MockChannelSender mockChannelSender
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.channelPolicyRepository = channelPolicyRepository;
        this.mockChannelSender = mockChannelSender;
    }

    @Transactional
    public void processQueuedEvent(Map<String, Object> event) {
        Long requestId = requiredLong(event, "requestId");
        String eventType = asString(event.get("eventType"), "ORDER");
        String priority = normalizePriority(asString(event.get("priority"), "NORMAL"));
        List<Long> receiverIds = asLongList(event.get("receiverIds"));
        if (receiverIds.isEmpty()) {
            throw new NonRetryableEventException(NonRetryableReasonCode.EMPTY_RECEIVER_IDS, "receiverIds must not be empty");
        }
        Map<String, Object> variables = asMap(event.get("variables"));
        boolean forceFail = Boolean.TRUE.equals(variables.get("forceFail"));
        String channel = "EMAIL";

        RetryPolicy retryPolicy = resolveRetryPolicy(eventType, channel);

        for (Long receiverId : receiverIds) {
            DeliveryTask task = getOrCreateTask(
                    requestId,
                    receiverId,
                    channel,
                    priority,
                    retryPolicy,
                    forceFail
            );
            if (task.getStatus() != DeliveryTaskStatus.PENDING) {
                log.info(
                        "[WORKER] duplicated queued event ignored. requestId={}, receiverId={}, channel={}, status={}",
                        requestId,
                        receiverId,
                        channel,
                        task.getStatus()
                );
                continue;
            }
            task.markSending();
            task = deliveryTaskRepository.save(task);
            executeSend(task);
        }
    }

    @Transactional
    public int processRetryBatch() {
        List<DeliveryTask> retryTargets = deliveryTaskRepository.findRetryTargetsForUpdate(
                DeliveryTaskStatus.FAILED.name(),
                LocalDateTime.now(),
                RETRY_BATCH_SIZE
        );

        for (DeliveryTask task : retryTargets) {
            task.markSending();
            executeSend(task);
        }
        return retryTargets.size();
    }

    private DeliveryTask getOrCreateTask(
            Long requestId,
            Long receiverId,
            String channel,
            String priority,
            RetryPolicy retryPolicy,
            boolean forceFail
    ) {
        return deliveryTaskRepository.findByRequestIdAndReceiverIdAndChannel(requestId, receiverId, channel)
                .orElseGet(() -> createTaskWithRaceGuard(requestId, receiverId, channel, priority, retryPolicy, forceFail));
    }

    private DeliveryTask createTaskWithRaceGuard(
            Long requestId,
            Long receiverId,
            String channel,
            String priority,
            RetryPolicy retryPolicy,
            boolean forceFail
    ) {
        try {
            return deliveryTaskRepository.save(DeliveryTask.create(
                    requestId,
                    receiverId,
                    channel,
                    priority,
                    retryPolicy.maxRetry(),
                    retryPolicy.backoffBaseSec(),
                    forceFail
            ));
        } catch (DataIntegrityViolationException ex) {
            return deliveryTaskRepository.findByRequestIdAndReceiverIdAndChannel(requestId, receiverId, channel)
                    .orElseThrow(() -> ex);
        }
    }

    private void executeSend(DeliveryTask task) {
        Instant startedAt = Instant.now();
        MockChannelSender.SendResult sendResult = task.isForceFail()
                ? new MockChannelSender.SendResult(false, null, "FORCED_FAIL", "Forced failure from request variable")
                : mockChannelSender.send(task);
        int latencyMs = (int) Duration.between(startedAt, Instant.now()).toMillis();

        if (sendResult.success()) {
            task.markSent();
        } else {
            task.markFailed(calculateNextRetryAt(task));
            if (task.shouldMoveToDlq()) {
                task.markDlq();
            }
        }

        deliveryTaskRepository.save(task);
        deliveryLogRepository.save(DeliveryLog.create(
                task.getId(),
                sendResult.providerMessageId(),
                sendResult.resultCode(),
                sendResult.resultMessage(),
                latencyMs
        ));
    }

    private Long asLong(Object value) {
        try {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } catch (RuntimeException ex) {
            throw new NonRetryableEventException(NonRetryableReasonCode.INVALID_NUMBER_VALUE, "Invalid number value: " + value, ex);
        }
    }

    private Long requiredLong(Map<String, Object> event, String field) {
        if (!event.containsKey(field) || event.get(field) == null) {
            throw new NonRetryableEventException(NonRetryableReasonCode.MISSING_REQUIRED_FIELD, field + " is required");
        }
        return asLong(event.get(field));
    }

    private String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private String normalizePriority(String priority) {
        String normalized = priority == null ? "" : priority.trim().toUpperCase();
        return switch (normalized) {
            case "HIGH", "NORMAL", "LOW" -> normalized;
            default -> throw new NonRetryableEventException(
                    NonRetryableReasonCode.INVALID_PRIORITY_VALUE,
                    "Invalid priority value: " + priority
            );
        };
    }

    private List<Long> asLongList(Object value) {
        if (value instanceof List<?> rawList) {
            List<Long> converted = new ArrayList<>(rawList.size());
            for (Object item : rawList) {
                converted.add(asLong(item));
            }
            return converted;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private RetryPolicy resolveRetryPolicy(String eventType, String channel) {
        return channelPolicyRepository.findFirstByEventTypeAndChannel(eventType, channel)
                .map(policy -> new RetryPolicy(policy.getMaxRetry(), policy.getBackoffBaseSec()))
                .orElse(new RetryPolicy(3, 2));
    }

    private LocalDateTime calculateNextRetryAt(DeliveryTask task) {
        int nextAttempt = task.getRetryCount() + 1;
        long backoffSeconds = (long) task.getBackoffBaseSec() * (1L << (nextAttempt - 1));
        return LocalDateTime.now().plusSeconds(backoffSeconds);
    }

    private record RetryPolicy(int maxRetry, int backoffBaseSec) {
    }
}
