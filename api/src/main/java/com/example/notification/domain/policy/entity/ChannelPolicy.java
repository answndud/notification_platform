package com.example.notification.domain.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "channel_policy")
public class ChannelPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "max_retry", nullable = false)
    private int maxRetry;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs;

    @Column(name = "backoff_base_sec", nullable = false)
    private int backoffBaseSec;
}
