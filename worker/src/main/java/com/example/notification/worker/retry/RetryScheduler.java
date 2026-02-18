package com.example.notification.worker.retry;

import com.example.notification.worker.service.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final NotificationDeliveryService notificationDeliveryService;

    public RetryScheduler(NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @Scheduled(fixedDelayString = "${notification.retry.poll-ms:5000}")
    public void pollAndRetry() {
        int processed = notificationDeliveryService.processRetryBatch();
        if (processed > 0) {
            log.info("[WORKER] retry batch processed count={}", processed);
        }
    }
}
