package com.example.notification.domain.task.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.global.exception.BusinessException;
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

    private DeliveryTaskService deliveryTaskService;

    @BeforeEach
    void setUp() {
        deliveryTaskService = new DeliveryTaskService(deliveryTaskRepository);
    }

    @Test
    void listThrowsWhenStatusInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, "UNKNOWN", null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenPageNegative() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, null, null, -1, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenPriorityInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, null, null, "urgent", 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenChannelInvalid() {
        assertThatThrownBy(() -> deliveryTaskService.list(null, "sms", null, null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void retryThrowsWhenTaskMissing() {
        when(deliveryTaskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryTaskService.retry(999L))
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

        var response = deliveryTaskService.list(1L, "email", "failed", "low", 0, 20);

        org.assertj.core.api.Assertions.assertThat(response.items()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(response.items().getFirst().priority()).isEqualTo("LOW");
    }
}
