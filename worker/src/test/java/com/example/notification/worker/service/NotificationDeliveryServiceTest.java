package com.example.notification.worker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.worker.domain.log.repository.DeliveryLogRepository;
import com.example.notification.worker.domain.policy.entity.ChannelPolicy;
import com.example.notification.worker.domain.policy.repository.ChannelPolicyRepository;
import com.example.notification.worker.domain.task.entity.DeliveryTask;
import com.example.notification.worker.domain.task.repository.DeliveryTaskRepository;
import com.example.notification.worker.consumer.NonRetryableEventException;
import com.example.notification.worker.consumer.NonRetryableReasonCode;
import com.example.notification.worker.sender.MockChannelSender;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;
    @Mock
    private DeliveryLogRepository deliveryLogRepository;
    @Mock
    private ChannelPolicyRepository channelPolicyRepository;
    @Mock
    private MockChannelSender mockChannelSender;

    private NotificationDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new NotificationDeliveryService(
                deliveryTaskRepository,
                deliveryLogRepository,
                channelPolicyRepository,
                mockChannelSender,
                50,
                86400
        );
    }

    @Test
    void failedSendUsesExponentialBackoff() {
        ChannelPolicy policy = mock(ChannelPolicy.class);
        when(policy.getMaxRetry()).thenReturn(3);
        when(policy.getBackoffBaseSec()).thenReturn(2);
        when(channelPolicyRepository.findFirstByEventTypeAndChannel("ORDER", "EMAIL")).thenReturn(Optional.of(policy));
        when(deliveryTaskRepository.findByRequestIdAndReceiverIdAndChannel(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deliveryLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.processQueuedEvent(Map.of(
                "requestId", 1L,
                "eventType", "ORDER",
                "priority", "HIGH",
                "receiverIds", List.of(1001L),
                "variables", Map.of("forceFail", true)
        ));

        ArgumentCaptor<DeliveryTask> captor = ArgumentCaptor.forClass(DeliveryTask.class);
        org.mockito.Mockito.verify(deliveryTaskRepository, org.mockito.Mockito.atLeast(2)).save(captor.capture());
        DeliveryTask savedAfterSend = captor.getAllValues().getLast();

        assertThat(savedAfterSend.getStatus().name()).isEqualTo("FAILED");
        assertThat(savedAfterSend.getRetryCount()).isEqualTo(1);
        assertThat(savedAfterSend.getNextRetryAt()).isAfter(LocalDateTime.now().plusSeconds(1));
        assertThat(savedAfterSend.getNextRetryAt()).isBefore(LocalDateTime.now().plusSeconds(4));
    }

    @Test
    void duplicateQueuedEventDoesNotSendTwice() {
        ChannelPolicy policy = mock(ChannelPolicy.class);
        when(policy.getMaxRetry()).thenReturn(3);
        when(policy.getBackoffBaseSec()).thenReturn(2);
        when(channelPolicyRepository.findFirstByEventTypeAndChannel("ORDER", "EMAIL")).thenReturn(Optional.of(policy));

        var savedTask = new java.util.concurrent.atomic.AtomicReference<DeliveryTask>();
        when(deliveryTaskRepository.findByRequestIdAndReceiverIdAndChannel(1L, 1001L, "EMAIL"))
                .thenAnswer(invocation -> Optional.ofNullable(savedTask.get()));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(invocation -> {
            DeliveryTask task = invocation.getArgument(0);
            savedTask.set(task);
            return task;
        });
        when(mockChannelSender.send(any(DeliveryTask.class)))
                .thenReturn(new MockChannelSender.SendResult(true, "mock-1", "SENT_OK", "ok"));
        when(deliveryLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> event = Map.of(
                "requestId", 1L,
                "eventType", "ORDER",
                "priority", "HIGH",
                "receiverIds", List.of(1001L),
                "variables", Map.of()
        );

        service.processQueuedEvent(event);
        service.processQueuedEvent(event);

        verify(mockChannelSender, times(1)).send(any(DeliveryTask.class));
        verify(deliveryLogRepository, times(1)).save(any());
    }

    @Test
    void processQueuedEventThrowsWhenRequestIdMissing() {
        assertThatThrownBy(() -> service.processQueuedEvent(Map.of(
                "eventType", "ORDER",
                "receiverIds", List.of(1001L)
        )))
                .isInstanceOf(NonRetryableEventException.class)
                .extracting(ex -> ((NonRetryableEventException) ex).getReasonCode())
                .isEqualTo(NonRetryableReasonCode.MISSING_REQUIRED_FIELD);
    }

    @Test
    void processQueuedEventThrowsWhenReceiverIdsEmpty() {
        assertThatThrownBy(() -> service.processQueuedEvent(Map.of(
                "requestId", 1L,
                "eventType", "ORDER",
                "receiverIds", List.of()
        )))
                .isInstanceOf(NonRetryableEventException.class)
                .extracting(ex -> ((NonRetryableEventException) ex).getReasonCode())
                .isEqualTo(NonRetryableReasonCode.EMPTY_RECEIVER_IDS);
    }

    @Test
    void processQueuedEventThrowsWhenRequestIdNotNumeric() {
        assertThatThrownBy(() -> service.processQueuedEvent(Map.of(
                "requestId", "abc",
                "eventType", "ORDER",
                "receiverIds", List.of(1001L)
        )))
                .isInstanceOf(NonRetryableEventException.class)
                .extracting(ex -> ((NonRetryableEventException) ex).getReasonCode())
                .isEqualTo(NonRetryableReasonCode.INVALID_NUMBER_VALUE);
    }

    @Test
    void processQueuedEventThrowsWhenPriorityInvalid() {
        assertThatThrownBy(() -> service.processQueuedEvent(Map.of(
                "requestId", 1L,
                "eventType", "ORDER",
                "priority", "URGENT",
                "receiverIds", List.of(1001L)
        )))
                .isInstanceOf(NonRetryableEventException.class)
                .extracting(ex -> ((NonRetryableEventException) ex).getReasonCode())
                .isEqualTo(NonRetryableReasonCode.INVALID_PRIORITY_VALUE);
    }

    @Test
    void processRetryBatchUsesConfiguredBatchSize() {
        when(deliveryTaskRepository.findRetryTargetsForUpdate(
                org.mockito.ArgumentMatchers.eq("FAILED"),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.eq(50)
        )).thenReturn(List.of());

        service.processRetryBatch();

        verify(deliveryTaskRepository).findRetryTargetsForUpdate(
                org.mockito.ArgumentMatchers.eq("FAILED"),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.eq(50)
        );
    }

    @Test
    void failedSendBackoffIsCappedByConfiguredMax() {
        service = new NotificationDeliveryService(
                deliveryTaskRepository,
                deliveryLogRepository,
                channelPolicyRepository,
                mockChannelSender,
                50,
                5
        );

        DeliveryTask existing = DeliveryTask.create(1L, 1001L, "EMAIL", "HIGH", 5, 4, true);
        existing.markSending();
        existing.markFailed(LocalDateTime.now().minusSeconds(1));

        when(deliveryTaskRepository.findRetryTargetsForUpdate(
                org.mockito.ArgumentMatchers.eq("FAILED"),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.eq(50)
        )).thenReturn(List.of(existing));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deliveryLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.processRetryBatch();

        assertThat(existing.getNextRetryAt()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(5));
    }
}
