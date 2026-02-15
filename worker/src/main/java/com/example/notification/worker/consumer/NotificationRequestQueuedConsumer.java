package com.example.notification.worker.consumer;

import com.example.notification.worker.service.NotificationDeliveryService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class NotificationRequestQueuedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestQueuedConsumer.class);
    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationRequestQueuedConsumer(NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.request-queued}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("[WORKER] queued event received: key={}, payload={}", key, payload);
        notificationDeliveryService.processQueuedEvent(payload);
    }
}
