package com.example.notification.worker.retry;

import com.example.notification.worker.service.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final NotificationDeliveryService notificationDeliveryService;
    private final boolean retryEnabled;

    public RetryScheduler(
            NotificationDeliveryService notificationDeliveryService,
            @Value("${notification.retry.enabled:true}") boolean retryEnabled
    ) {
        this.notificationDeliveryService = notificationDeliveryService;
        this.retryEnabled = retryEnabled;
    }

    @Scheduled(fixedDelayString = "${notification.retry.poll-ms:5000}")
    public void pollAndRetry() {
        if (!retryEnabled) {
            return;
        }

        try {
            int processed = notificationDeliveryService.processRetryBatch();
            if (processed > 0) {
                log.info("[WORKER] retry batch processed count={}", processed);
            }
        } catch (Exception ex) {
            log.error("[WORKER] retry scheduler loop failed", ex);
        }
    }
}
