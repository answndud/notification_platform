package com.example.notification.worker.consumer;

import com.example.notification.worker.service.NotificationDeliveryService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class NotificationRequestQueuedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestQueuedConsumer.class);
    private final NotificationDeliveryService notificationDeliveryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String malformedTopic;

    public NotificationRequestQueuedConsumer(
            NotificationDeliveryService notificationDeliveryService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${notification.kafka.topics.request-queued-malformed:notification.request.queued.malformed.v1}") String malformedTopic
    ) {
        this.notificationDeliveryService = notificationDeliveryService;
        this.kafkaTemplate = kafkaTemplate;
        this.malformedTopic = malformedTopic;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.request-queued}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("[WORKER] queued event received: key={}, payload={}", key, payload);
        try {
            notificationDeliveryService.processQueuedEvent(payload);
            acknowledgment.acknowledge();
        } catch (NonRetryableEventException ex) {
            log.error("[WORKER] non-retryable queued event skipped: key={}, reason={}", key, ex.getMessage());
            publishMalformedEvent(key, payload, topic, partition, offset, ex);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("[WORKER] queued event processing failed: key={}", key, ex);
            throw ex;
        }
    }

    private void publishMalformedEvent(
            String key,
            Map<String, Object> payload,
            String topic,
            int partition,
            long offset,
            NonRetryableEventException ex
    ) {
        String safeKey = key == null ? "unknown" : key;
        Map<String, Object> malformedEvent = new LinkedHashMap<>();
        malformedEvent.put("sourceTopic", topic);
        malformedEvent.put("sourcePartition", partition);
        malformedEvent.put("sourceOffset", offset);
        malformedEvent.put("originalKey", safeKey);
        malformedEvent.put("reasonCode", ex.getReasonCode().name());
        malformedEvent.put("reason", ex.getMessage());
        malformedEvent.put("payload", payload == null ? Map.of() : payload);
        malformedEvent.put("occurredAt", Instant.now().toString());

        try {
            kafkaTemplate.send(malformedTopic, safeKey, malformedEvent);
        } catch (Exception publishEx) {
            log.error("[WORKER] malformed event publish failed: key={}, topic={}", safeKey, malformedTopic, publishEx);
        }
    }
}
