package com.example.notification.domain.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryLogRepository;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.metrics.dto.NotificationMetricsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationMetricsServiceTest {

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;
    @Mock
    private DeliveryLogRepository deliveryLogRepository;
    @Mock
    private KafkaLagService kafkaLagService;

    private NotificationMetricsService notificationMetricsService;

    @BeforeEach
    void setUp() {
        notificationMetricsService = new NotificationMetricsService(
                deliveryTaskRepository,
                deliveryLogRepository,
                kafkaLagService
        );
    }

    @Test
    void getMetricsReturnsTaskCountsLagAndRates() {
        when(deliveryTaskRepository.countByStatus(DeliveryTaskStatus.PENDING)).thenReturn(2L);
        when(deliveryTaskRepository.countByStatus(DeliveryTaskStatus.SENDING)).thenReturn(1L);
        when(deliveryTaskRepository.countByStatus(DeliveryTaskStatus.SENT)).thenReturn(7L);
        when(deliveryTaskRepository.countByStatus(DeliveryTaskStatus.FAILED)).thenReturn(2L);
        when(deliveryTaskRepository.countByStatus(DeliveryTaskStatus.DLQ)).thenReturn(1L);
        when(deliveryLogRepository.findAverageLatencyMs()).thenReturn(18.237d);
        when(kafkaLagService.getRequestQueuedLag()).thenReturn(12L);

        NotificationMetricsResponse response = notificationMetricsService.getMetrics();

        assertThat(response.pendingTasks()).isEqualTo(2L);
        assertThat(response.sendingTasks()).isEqualTo(1L);
        assertThat(response.sentTasks()).isEqualTo(7L);
        assertThat(response.failedTasks()).isEqualTo(2L);
        assertThat(response.dlqTasks()).isEqualTo(1L);
        assertThat(response.requestQueuedLag()).isEqualTo(12L);
        assertThat(response.successRate()).isEqualTo(70.0);
        assertThat(response.averageLatencyMs()).isEqualTo(18.24);
    }
}
