package com.example.notification.worker.service;

import com.example.notification.worker.domain.log.entity.DeliveryLog;
import com.example.notification.worker.domain.log.repository.DeliveryLogRepository;
import com.example.notification.worker.domain.task.entity.DeliveryTask;
import com.example.notification.worker.domain.task.repository.DeliveryTaskRepository;
import com.example.notification.worker.sender.MockChannelSender;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final MockChannelSender mockChannelSender;

    public NotificationDeliveryService(
            DeliveryTaskRepository deliveryTaskRepository,
            DeliveryLogRepository deliveryLogRepository,
            MockChannelSender mockChannelSender
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.mockChannelSender = mockChannelSender;
    }

    @Transactional
    public void processQueuedEvent(Map<String, Object> event) {
        Long requestId = asLong(event.get("requestId"));
        String priority = asString(event.get("priority"), "NORMAL");
        List<Long> receiverIds = asLongList(event.get("receiverIds"));

        for (Long receiverId : receiverIds) {
            DeliveryTask task = DeliveryTask.create(requestId, receiverId, "EMAIL", priority, 3);
            task.markSending();
            task = deliveryTaskRepository.save(task);

            Instant startedAt = Instant.now();
            MockChannelSender.SendResult sendResult = mockChannelSender.send(task);
            int latencyMs = (int) Duration.between(startedAt, Instant.now()).toMillis();

            if (sendResult.success()) {
                task.markSent();
            } else {
                task.markFailed();
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
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
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
}
