CREATE TABLE notification_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(80) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    body_template VARCHAR(2000) NOT NULL,
    default_channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_notification_template_code UNIQUE (template_code)
);

CREATE TABLE receiver (
    id BIGINT PRIMARY KEY,
    receiver_type VARCHAR(20) NOT NULL,
    email VARCHAR(120),
    phone VARCHAR(40),
    push_token VARCHAR(200),
    timezone VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE channel_policy (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    max_retry INT NOT NULL,
    timeout_ms INT NOT NULL,
    backoff_base_sec INT NOT NULL
);

CREATE TABLE delivery_task (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL,
    max_retry INT NOT NULL,
    next_retry_at TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE delivery_log (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    provider_message_id VARCHAR(120),
    result_code VARCHAR(40) NOT NULL,
    result_message VARCHAR(500),
    latency_ms INT,
    logged_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_delivery_task_status_next_retry_at
    ON delivery_task (status, next_retry_at);

CREATE INDEX idx_delivery_task_request_id
    ON delivery_task (request_id);

CREATE INDEX idx_delivery_log_task_id_logged_at
    ON delivery_log (task_id, logged_at);
