package com.example.notification.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "N400", "유효하지 않은 입력입니다."),
    DUPLICATE_REQUEST_KEY(HttpStatus.CONFLICT, "N409", "이미 처리된 requestKey 입니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "N404_TEMPLATE", "알림 템플릿을 찾을 수 없습니다."),
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "N404_RECEIVER", "알림 수신자를 찾을 수 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "N404", "알림 요청을 찾을 수 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "N404_TASK", "알림 작업을 찾을 수 없습니다."),
    DLQ_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "N404_DLQ", "DLQ 작업을 찾을 수 없습니다."),
    DLQ_REPLAY_NOT_ALLOWED(HttpStatus.CONFLICT, "N409_DLQ_REPLAY", "DLQ 상태에서만 재처리할 수 있습니다."),
    TASK_RETRY_NOT_ALLOWED(HttpStatus.CONFLICT, "N409_TASK_RETRY", "FAILED 또는 DLQ 상태에서만 즉시 재시도할 수 있습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N500", "내부 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
