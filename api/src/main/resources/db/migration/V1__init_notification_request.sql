CREATE TABLE notification_request (
    id BIGSERIAL PRIMARY KEY,
    request_key VARCHAR(120) NOT NULL,
    template_code VARCHAR(80) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_request_key UNIQUE (request_key)
);

CREATE INDEX idx_notification_request_status_requested_at
    ON notification_request (status, requested_at DESC);
