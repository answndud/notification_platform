package com.example.notification.worker.domain.task.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DeliveryTaskTest {

    @Test
    void transitionsToSentFromSending() {
        DeliveryTask task = DeliveryTask.create(1L, 1001L, "EMAIL", "HIGH", 3, 2, false);
        task.markSending();
        task.markSent();

        assertThat(task.shouldMoveToDlq()).isFalse();
    }

    @Test
    void movesToDlqWhenRetryExceeded() {
        DeliveryTask task = DeliveryTask.create(1L, 1001L, "EMAIL", "HIGH", 1, 2, true);
        task.markSending();
        task.markFailed(LocalDateTime.now().plusSeconds(5));

        assertThat(task.shouldMoveToDlq()).isTrue();
        task.markDlq();
    }

    @Test
    void throwsOnInvalidTransition() {
        DeliveryTask task = DeliveryTask.create(1L, 1001L, "EMAIL", "HIGH", 3, 2, false);

        assertThatThrownBy(task::markSent)
                .isInstanceOf(IllegalStateException.class);
    }
}
