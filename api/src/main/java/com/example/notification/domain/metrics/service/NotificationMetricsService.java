package com.example.notification.domain.metrics.service;

import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryLogRepository;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.metrics.dto.NotificationMetricsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationMetricsService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final KafkaLagService kafkaLagService;

    public NotificationMetricsService(
            DeliveryTaskRepository deliveryTaskRepository,
            DeliveryLogRepository deliveryLogRepository,
            KafkaLagService kafkaLagService
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.kafkaLagService = kafkaLagService;
    }

    public NotificationMetricsResponse getMetrics() {
        long pending = deliveryTaskRepository.countByStatus(DeliveryTaskStatus.PENDING);
        long sending = deliveryTaskRepository.countByStatus(DeliveryTaskStatus.SENDING);
        long sent = deliveryTaskRepository.countByStatus(DeliveryTaskStatus.SENT);
        long failed = deliveryTaskRepository.countByStatus(DeliveryTaskStatus.FAILED);
        long dlq = deliveryTaskRepository.countByStatus(DeliveryTaskStatus.DLQ);
        long requestQueuedLag = kafkaLagService.getRequestQueuedLag();

        long done = sent + failed + dlq;
        double successRate = done == 0 ? 0.0 : ((double) sent / done) * 100.0;
        Double avgLatency = deliveryLogRepository.findAverageLatencyMs();
        double averageLatencyMs = avgLatency == null ? 0.0 : avgLatency;

        return new NotificationMetricsResponse(
                pending,
                sending,
                sent,
                failed,
                dlq,
                requestQueuedLag,
                roundTwo(successRate),
                roundTwo(averageLatencyMs)
        );
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
