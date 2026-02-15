package com.example.notification.worker.sender;

import com.example.notification.worker.domain.task.entity.DeliveryTask;

public interface MockChannelSender {

    SendResult send(DeliveryTask task);

    record SendResult(boolean success, String providerMessageId, String resultCode, String resultMessage) {
    }
}
