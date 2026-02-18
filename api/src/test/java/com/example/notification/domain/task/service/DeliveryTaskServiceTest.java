package com.example.notification.domain.task.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
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
class DeliveryTaskServiceTest {

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    private DeliveryTaskService deliveryTaskService;

    @BeforeEach
    void setUp() {
        deliveryTaskService = new DeliveryTaskService(deliveryTaskRepository, notificationRequestRepository);
    }

    @Test
    void listThrowsWhenStatusInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, null, "UNKNOWN", null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenPageNegative() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, null, null, null, -1, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenPriorityInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, null, null, "urgent", 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenChannelInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, "sms", null, null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listReturnsEmptyWhenRequestKeyNotFound() {
        when(notificationRequestRepository.findByRequestKey("missing")).thenReturn(Optional.empty());

        var response = deliveryTaskService.list(null, "missing", null, null, null, 0, 20);

        org.assertj.core.api.Assertions.assertThat(response.items()).isEmpty();
        org.assertj.core.api.Assertions.assertThat(response.totalElements()).isZero();
    }

    @Test
    void retryThrowsWhenTaskMissing() {
        when(deliveryTaskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryTaskService.retry(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void retryThrowsWhenTaskRetryNotAllowed() {
        DeliveryTask task = org.mockito.Mockito.mock(DeliveryTask.class);
        when(deliveryTaskRepository.findById(15L)).thenReturn(Optional.of(task));
        doThrow(new BusinessException(ErrorCode.TASK_RETRY_NOT_ALLOWED)).when(task).retryNow();

        assertThatThrownBy(() -> deliveryTaskService.retry(15L))
                .isInstanceOf(BusinessException.class);

        verify(deliveryTaskRepository, never()).save(task);
    }

    @Test
    void getThrowsWhenTaskMissing() {
        when(deliveryTaskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryTaskService.get(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listMapsPriorityInResponse() {
        DeliveryTask task = org.mockito.Mockito.mock(DeliveryTask.class);
        when(task.getId()).thenReturn(10L);
        when(task.getRequestId()).thenReturn(1L);
        when(task.getReceiverId()).thenReturn(1001L);
        when(task.getChannel()).thenReturn("EMAIL");
        when(task.getPriority()).thenReturn("LOW");
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.FAILED);
        when(task.getRetryCount()).thenReturn(2);
        when(task.getMaxRetry()).thenReturn(3);
        when(task.getNextRetryAt()).thenReturn(LocalDateTime.of(2026, 2, 18, 10, 0));
        when(task.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 2, 18, 9, 0));

        when(deliveryTaskRepository.findWithFilters(DeliveryTaskStatus.FAILED, 1L, "EMAIL", "LOW", PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1));

        NotificationRequest request = org.mockito.Mockito.mock(NotificationRequest.class);
        when(request.getId()).thenReturn(1L);
        when(notificationRequestRepository.findByRequestKey("order-1")).thenReturn(Optional.of(request));

        var response = deliveryTaskService.list(1L, "order-1", "email", "failed", "low", 0, 20);

        org.assertj.core.api.Assertions.assertThat(response.items()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(response.items().getFirst().priority()).isEqualTo("LOW");
    }

    @Test
    void getMapsPriorityInResponse() {
        DeliveryTask task = org.mockito.Mockito.mock(DeliveryTask.class);
        when(task.getId()).thenReturn(11L);
        when(task.getRequestId()).thenReturn(2L);
        when(task.getReceiverId()).thenReturn(2001L);
        when(task.getChannel()).thenReturn("EMAIL");
        when(task.getPriority()).thenReturn("HIGH");
        when(task.getStatus()).thenReturn(DeliveryTaskStatus.SENT);
        when(task.getRetryCount()).thenReturn(0);
        when(task.getMaxRetry()).thenReturn(3);
        when(task.getNextRetryAt()).thenReturn(null);
        when(task.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 2, 18, 14, 0));
        when(deliveryTaskRepository.findById(11L)).thenReturn(Optional.of(task));

        var response = deliveryTaskService.get(11L);

        org.assertj.core.api.Assertions.assertThat(response.priority()).isEqualTo("HIGH");
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo("SENT");
    }
}
