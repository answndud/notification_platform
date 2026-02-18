package com.example.notification.worker.consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.example.notification.worker.service.NotificationDeliveryService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class NotificationRequestQueuedConsumerTest {

    @Mock
    private NotificationDeliveryService notificationDeliveryService;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private Acknowledgment acknowledgment;

    private NotificationRequestQueuedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new NotificationRequestQueuedConsumer(
                notificationDeliveryService,
                kafkaTemplate,
                "notification.request.queued.malformed.v1"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void consumeAcknowledgesWhenEventIsNonRetryable() {
        doThrow(new NonRetryableEventException("requestId is required"))
                .when(notificationDeliveryService)
                .processQueuedEvent(Map.of("receiverIds", java.util.List.of(1L)));

        consumer.consume(
                Map.of("receiverIds", java.util.List.of(1L)),
                "bad-key",
                "notification.request.queued.v1",
                0,
                42L,
                acknowledgment
        );

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(
                eq("notification.request.queued.malformed.v1"),
                eq("bad-key"),
                captor.capture()
        );
        Map<String, Object> published = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(published.get("sourceTopic")).isEqualTo("notification.request.queued.v1");
        org.assertj.core.api.Assertions.assertThat(published.get("sourcePartition")).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(published.get("sourceOffset")).isEqualTo(42L);
        org.assertj.core.api.Assertions.assertThat(published.get("reasonCode")).isEqualTo("INVALID_EVENT");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void consumeRethrowsUnexpectedException() {
        doThrow(new IllegalStateException("temporary failure"))
                .when(notificationDeliveryService)
                .processQueuedEvent(Map.of("requestId", 1L, "receiverIds", java.util.List.of(1L)));

        assertThatThrownBy(() -> consumer.consume(
                Map.of("requestId", 1L, "receiverIds", java.util.List.of(1L)),
                "key-1",
                "notification.request.queued.v1",
                1,
                99L,
                acknowledgment
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void consumePublishesTypedReasonCodeForMalformedPayload() {
        doThrow(new NonRetryableEventException(
                NonRetryableReasonCode.INVALID_PRIORITY_VALUE,
                "Invalid priority value: URGENT"
        )).when(notificationDeliveryService)
                .processQueuedEvent(Map.of("requestId", 1L, "receiverIds", java.util.List.of(1L), "priority", "URGENT"));

        consumer.consume(
                Map.of("requestId", 1L, "receiverIds", java.util.List.of(1L), "priority", "URGENT"),
                "bad-priority",
                "notification.request.queued.v1",
                3,
                101L,
                acknowledgment
        );

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(
                eq("notification.request.queued.malformed.v1"),
                eq("bad-priority"),
                captor.capture()
        );
        Map<String, Object> published = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(published.get("reasonCode")).isEqualTo("INVALID_PRIORITY_VALUE");
        verify(acknowledgment).acknowledge();
    }
}
