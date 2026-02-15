package com.example.notification.domain.request.producer;

public interface NotificationRequestEventProducer {

    void publish(NotificationRequestQueuedEvent event);
}
