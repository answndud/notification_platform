package com.example.notification.worker.domain.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_log")
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "provider_message_id", length = 120)
    private String providerMessageId;

    @Column(name = "result_code", nullable = false, length = 40)
    private String resultCode;

    @Column(name = "result_message", length = 500)
    private String resultMessage;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    protected DeliveryLog() {
    }

    private DeliveryLog(Long taskId, String providerMessageId, String resultCode, String resultMessage, Integer latencyMs) {
        this.taskId = taskId;
        this.providerMessageId = providerMessageId;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.latencyMs = latencyMs;
        this.loggedAt = LocalDateTime.now();
    }

    public static DeliveryLog create(
            Long taskId,
            String providerMessageId,
            String resultCode,
            String resultMessage,
            Integer latencyMs
    ) {
        return new DeliveryLog(taskId, providerMessageId, resultCode, resultMessage, latencyMs);
    }
}
