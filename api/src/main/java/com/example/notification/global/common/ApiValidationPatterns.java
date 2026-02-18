package com.example.notification.global.common;

public final class ApiValidationPatterns {

    public static final String PRIORITY = "(?i)HIGH|NORMAL|LOW";
    public static final String CHANNEL = "(?i)EMAIL";
    public static final String REQUEST_STATUS = "(?i)PENDING|QUEUED|PROCESSING|COMPLETED|FAILED";
    public static final String TASK_STATUS = "(?i)PENDING|SENDING|SENT|FAILED|DLQ";

    private ApiValidationPatterns() {
    }
}
