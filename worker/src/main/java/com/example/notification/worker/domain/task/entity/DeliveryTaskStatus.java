package com.example.notification.worker.domain.task.entity;

public enum DeliveryTaskStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    DLQ
}
