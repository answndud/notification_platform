package com.example.notification.domain.request.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_request", uniqueConstraints = {
        @UniqueConstraint(name = "uk_request_key", columnNames = "request_key")
})
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_key", nullable = false, length = 120)
    private String requestKey;

    @Column(name = "template_code", nullable = false, length = 80)
    private String templateCode;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    protected NotificationRequest() {
    }

    private NotificationRequest(String requestKey, String templateCode, String priority) {
        this.requestKey = requestKey;
        this.templateCode = templateCode;
        this.priority = priority;
        this.status = NotificationRequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public static NotificationRequest create(String requestKey, String templateCode, String priority) {
        return new NotificationRequest(requestKey, templateCode, priority);
    }

    public void markQueued() {
        this.status = NotificationRequestStatus.QUEUED;
    }

    public Long getId() {
        return id;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getPriority() {
        return priority;
    }

    public NotificationRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
