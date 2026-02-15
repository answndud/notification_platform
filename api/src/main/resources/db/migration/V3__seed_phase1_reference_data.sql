INSERT INTO notification_template (
    template_code,
    event_type,
    title_template,
    body_template,
    default_channel,
    status,
    created_at
)
VALUES
    ('ORDER_PAID', 'ORDER', '주문 결제 완료', '주문 {{orderNo}} 결제가 완료되었습니다.', 'EMAIL', 'ACTIVE', NOW())
ON CONFLICT (template_code) DO NOTHING;

INSERT INTO receiver (
    id,
    receiver_type,
    email,
    phone,
    push_token,
    timezone,
    active
)
VALUES
    (1001, 'PARENT', 'parent1001@example.com', NULL, NULL, 'Asia/Seoul', true),
    (1002, 'PARENT', 'parent1002@example.com', NULL, NULL, 'Asia/Seoul', true),
    (1003, 'PARENT', 'parent1003@example.com', NULL, NULL, 'Asia/Seoul', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO channel_policy (
    event_type,
    channel,
    max_retry,
    timeout_ms,
    backoff_base_sec
)
VALUES
    ('ORDER', 'EMAIL', 3, 2000, 2);
