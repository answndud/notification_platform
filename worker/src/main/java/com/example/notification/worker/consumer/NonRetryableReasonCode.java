package com.example.notification.worker.consumer;

public enum NonRetryableReasonCode {
    INVALID_EVENT,
    MISSING_REQUIRED_FIELD,
    EMPTY_RECEIVER_IDS,
    INVALID_NUMBER_VALUE,
    INVALID_PRIORITY_VALUE
}
