package com.example.notification.domain.dlq.entity;

public enum DeliveryTaskStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    DLQ
}
