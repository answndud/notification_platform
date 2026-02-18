package com.example.notification.worker.consumer;

public class NonRetryableEventException extends RuntimeException {

    private final NonRetryableReasonCode reasonCode;

    public NonRetryableEventException(NonRetryableReasonCode reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public NonRetryableEventException(NonRetryableReasonCode reasonCode, String message, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
    }

    public NonRetryableEventException(String message) {
        this(NonRetryableReasonCode.INVALID_EVENT, message);
    }

    public NonRetryableEventException(String message, Throwable cause) {
        this(NonRetryableReasonCode.INVALID_EVENT, message, cause);
    }

    public NonRetryableReasonCode getReasonCode() {
        return reasonCode;
    }
}
