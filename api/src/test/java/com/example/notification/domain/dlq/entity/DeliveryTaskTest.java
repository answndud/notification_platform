package com.example.notification.domain.dlq.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.notification.global.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DeliveryTaskTest {

    @Test
    void replayNowResetsRetryAndForceFail() {
        DeliveryTask task = new DeliveryTask();
        ReflectionTestUtils.setField(task, "status", DeliveryTaskStatus.DLQ);
        ReflectionTestUtils.setField(task, "retryCount", 3);
        ReflectionTestUtils.setField(task, "forceFail", true);

        task.replayNow();

        assertThat(task.getStatus()).isEqualTo(DeliveryTaskStatus.FAILED);
        assertThat(task.getRetryCount()).isZero();
        assertThat(task.getNextRetryAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void replayNowThrowsWhenStatusIsNotDlq() {
        DeliveryTask task = new DeliveryTask();
        ReflectionTestUtils.setField(task, "status", DeliveryTaskStatus.FAILED);

        assertThatThrownBy(task::replayNow)
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void retryNowFromFailedSetsImmediateRetryTime() {
        DeliveryTask task = new DeliveryTask();
        ReflectionTestUtils.setField(task, "status", DeliveryTaskStatus.FAILED);

        task.retryNow();

        assertThat(task.getStatus()).isEqualTo(DeliveryTaskStatus.FAILED);
        assertThat(task.getNextRetryAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void retryNowFromDlqResetsRetryAndForceFail() {
        DeliveryTask task = new DeliveryTask();
        ReflectionTestUtils.setField(task, "status", DeliveryTaskStatus.DLQ);
        ReflectionTestUtils.setField(task, "retryCount", 3);
        ReflectionTestUtils.setField(task, "forceFail", true);

        task.retryNow();

        assertThat(task.getStatus()).isEqualTo(DeliveryTaskStatus.FAILED);
        assertThat(task.getRetryCount()).isZero();
        assertThat(task.getNextRetryAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }
}
