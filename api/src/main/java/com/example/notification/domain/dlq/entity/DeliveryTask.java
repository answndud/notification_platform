package com.example.notification.domain.dlq.entity;

import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
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

    @Column(name = "force_fail", nullable = false)
    private boolean forceFail;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected DeliveryTask() {
    }

    public void replayNow() {
        if (status != DeliveryTaskStatus.DLQ) {
            throw new BusinessException(ErrorCode.DLQ_REPLAY_NOT_ALLOWED);
        }
        this.retryCount = 0;
        this.forceFail = false;
        this.status = DeliveryTaskStatus.FAILED;
        this.nextRetryAt = LocalDateTime.now();
    }

    public void retryNow() {
        if (status != DeliveryTaskStatus.FAILED && status != DeliveryTaskStatus.DLQ) {
            throw new BusinessException(ErrorCode.TASK_RETRY_NOT_ALLOWED);
        }
        if (status == DeliveryTaskStatus.DLQ) {
            this.retryCount = 0;
            this.forceFail = false;
            this.status = DeliveryTaskStatus.FAILED;
        }
        this.nextRetryAt = LocalDateTime.now();
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

    public String getPriority() {
        return priority;
    }

    public DeliveryTaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }
}
