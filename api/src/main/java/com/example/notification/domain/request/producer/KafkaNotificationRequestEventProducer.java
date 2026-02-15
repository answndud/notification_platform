package com.example.notification.domain.request.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationRequestEventProducer implements NotificationRequestEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String requestQueuedTopic;

    public KafkaNotificationRequestEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${notification.kafka.topics.request-queued}") String requestQueuedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestQueuedTopic = requestQueuedTopic;
    }

    @Override
    public void publish(NotificationRequestQueuedEvent event) {
        kafkaTemplate.send(requestQueuedTopic, event.requestKey(), event);
    }
}
