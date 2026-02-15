package com.example.notification.worker.domain.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_task")
public class DeliveryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryTaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retry", nullable = false)
    private int maxRetry;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected DeliveryTask() {
    }

    private DeliveryTask(Long requestId, Long receiverId, String channel, String priority, int maxRetry) {
        this.requestId = requestId;
        this.receiverId = receiverId;
        this.channel = channel;
        this.priority = priority;
        this.status = DeliveryTaskStatus.PENDING;
        this.retryCount = 0;
        this.maxRetry = maxRetry;
        this.createdAt = LocalDateTime.now();
    }

    public static DeliveryTask create(Long requestId, Long receiverId, String channel, String priority, int maxRetry) {
        return new DeliveryTask(requestId, receiverId, channel, priority, maxRetry);
    }

    public void markSending() {
        this.status = DeliveryTaskStatus.SENDING;
    }

    public void markSent() {
        this.status = DeliveryTaskStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = DeliveryTaskStatus.FAILED;
    }

    public Long getId() {
        return id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getChannel() {
        return channel;
    }
}
