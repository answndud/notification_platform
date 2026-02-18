package com.example.notification.worker.retry;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.worker.service.NotificationDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetrySchedulerTest {

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @Test
    void pollAndRetrySkipsWhenDisabled() {
        RetryScheduler scheduler = new RetryScheduler(notificationDeliveryService, false);

        scheduler.pollAndRetry();

        verify(notificationDeliveryService, never()).processRetryBatch();
    }

    @Test
    void pollAndRetryProcessesWhenEnabled() {
        RetryScheduler scheduler = new RetryScheduler(notificationDeliveryService, true);
        when(notificationDeliveryService.processRetryBatch()).thenReturn(3);

        scheduler.pollAndRetry();

        verify(notificationDeliveryService).processRetryBatch();
    }

    @Test
    void pollAndRetrySwallowsServiceException() {
        RetryScheduler scheduler = new RetryScheduler(notificationDeliveryService, true);
        when(notificationDeliveryService.processRetryBatch()).thenThrow(new IllegalStateException("db down"));

        scheduler.pollAndRetry();

        verify(notificationDeliveryService).processRetryBatch();
    }
}
