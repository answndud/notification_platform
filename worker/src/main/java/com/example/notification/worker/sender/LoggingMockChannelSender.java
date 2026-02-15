package com.example.notification.worker.sender;

import com.example.notification.worker.domain.task.entity.DeliveryTask;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoggingMockChannelSender implements MockChannelSender {

    @Override
    public SendResult send(DeliveryTask task) {
        return new SendResult(
                true,
                "mock-" + UUID.randomUUID(),
                "SENT_OK",
                "Mock provider accepted"
        );
    }
}
