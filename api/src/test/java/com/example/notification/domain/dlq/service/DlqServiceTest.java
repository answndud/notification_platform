package com.example.notification.domain.dlq.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.dlq.dto.DlqListResponse;
import com.example.notification.domain.dlq.dto.DlqReplayResponse;
import com.example.notification.domain.dlq.entity.DeliveryLog;
import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryLogRepository;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DlqServiceTest {

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;
    @Mock
    private DeliveryLogRepository deliveryLogRepository;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    private DlqService dlqService;

    @BeforeEach
    void setUp() {
        dlqService = new DlqService(deliveryTaskRepository, deliveryLogRepository, notificationRequestRepository);
    }

    @Test
    void getDlqTasksReturnsPage() {
        DeliveryTask task = mock(DeliveryTask.class);
        DeliveryLog log = mock(DeliveryLog.class);
        when(task.getId()).thenReturn(10L);
        when(task.getRequestId()).thenReturn(100L);
        when(task.getReceiverId()).thenReturn(1001L);
        when(task.getChannel()).thenReturn("EMAIL");
        when(task.getPriority()).thenReturn("HIGH");
        when(task.getRetryCount()).thenReturn(3);
        when(task.getMaxRetry()).thenReturn(3);
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.DLQ);
        when(task.getNextRetryAt()).thenReturn(LocalDateTime.of(2026, 2, 15, 10, 5));
        when(task.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 2, 15, 10, 0));
        when(log.getResultCode()).thenReturn("FORCED_FAIL");
        when(log.getResultMessage()).thenReturn("Forced failure");

        when(deliveryTaskRepository.findWithFilters(DeliveryTaskStatus.DLQ, 100L, "EMAIL", "HIGH", PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1));
        when(deliveryLogRepository.findTopByTaskIdOrderByLoggedAtDescIdDesc(10L)).thenReturn(Optional.of(log));

        NotificationRequest request = mock(NotificationRequest.class);
        when(request.getId()).thenReturn(100L);
        when(notificationRequestRepository.findByRequestKey("order-100")).thenReturn(Optional.of(request));

        DlqListResponse response = dlqService.getDlqTasks(100L, "order-100", "email", "high", 0, 20);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().lastResultCode()).isEqualTo("FORCED_FAIL");
        assertThat(response.items().getFirst().priority()).isEqualTo("HIGH");
        assertThat(response.items().getFirst().status()).isEqualTo("DLQ");
    }

    @Test
    void replayThrowsWhenTaskMissing() {
        when(deliveryTaskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dlqService.replay(99L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getThrowsWhenTaskMissing() {
        when(deliveryTaskRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dlqService.get(77L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getThrowsWhenTaskNotDlqStatus() {
        DeliveryTask task = mock(DeliveryTask.class);
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.FAILED);

        assertThatThrownBy(() -> dlqService.get(10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void replayReturnsUpdatedTask() {
        DeliveryTask task = mock(DeliveryTask.class);
        LocalDateTime retryAt = LocalDateTime.now();
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(task.getId()).thenReturn(10L);
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.FAILED);
        when(task.getRetryCount()).thenReturn(0);
        when(task.getNextRetryAt()).thenReturn(retryAt);

        DlqReplayResponse response = dlqService.replay(10L);

        verify(task).replayNow();
        verify(deliveryTaskRepository).save(task);
        assertThat(response.taskId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo("FAILED");
    }

    @Test
    void replayThrowsWhenReplayNotAllowed() {
        DeliveryTask task = mock(DeliveryTask.class);
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        doThrow(new BusinessException(ErrorCode.DLQ_REPLAY_NOT_ALLOWED)).when(task).replayNow();

        assertThatThrownBy(() -> dlqService.replay(10L))
                .isInstanceOf(BusinessException.class);

        verify(deliveryTaskRepository, never()).save(task);
    }

    @Test
    void getReturnsTaskDetailsWithLatestLog() {
        DeliveryTask task = mock(DeliveryTask.class);
        DeliveryLog log = mock(DeliveryLog.class);
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.DLQ);
        when(task.getId()).thenReturn(10L);
        when(task.getRequestId()).thenReturn(100L);
        when(task.getReceiverId()).thenReturn(1001L);
        when(task.getChannel()).thenReturn("EMAIL");
        when(task.getPriority()).thenReturn("HIGH");
        when(task.getRetryCount()).thenReturn(3);
        when(task.getMaxRetry()).thenReturn(3);
        when(task.getNextRetryAt()).thenReturn(LocalDateTime.of(2026, 2, 18, 15, 10));
        when(task.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 2, 18, 15, 0));
        when(log.getResultCode()).thenReturn("FORCED_FAIL");
        when(log.getResultMessage()).thenReturn("Forced failure");
        when(deliveryLogRepository.findTopByTaskIdOrderByLoggedAtDescIdDesc(10L)).thenReturn(Optional.of(log));

        var response = dlqService.get(10L);

        assertThat(response.taskId()).isEqualTo(10L);
        assertThat(response.priority()).isEqualTo("HIGH");
        assertThat(response.status()).isEqualTo("DLQ");
        assertThat(response.nextRetryAt()).isEqualTo(LocalDateTime.of(2026, 2, 18, 15, 10));
        assertThat(response.lastResultCode()).isEqualTo("FORCED_FAIL");
    }

    @Test
    void getDlqTasksThrowsWhenSizeOutOfRange() {
        assertThatThrownBy(() -> dlqService.getDlqTasks(null, null, null, null, 0, 0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getDlqTasksThrowsWhenPriorityInvalid() {
        assertThatThrownBy(() -> dlqService.getDlqTasks(null, null, null, "urgent", 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getDlqTasksThrowsWhenChannelInvalid() {
        assertThatThrownBy(() -> dlqService.getDlqTasks(null, null, "sms", null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getDlqTasksReturnsEmptyWhenRequestKeyNotFound() {
        when(notificationRequestRepository.findByRequestKey("missing")).thenReturn(Optional.empty());

        var response = dlqService.getDlqTasks(null, "missing", null, null, 0, 20);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalElements()).isZero();
    }
}
