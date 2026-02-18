package com.example.notification.domain.request.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.receiver.repository.ReceiverRepository;
import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.producer.NotificationRequestEventProducer;
import com.example.notification.domain.request.producer.NotificationRequestQueuedEvent;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.template.entity.NotificationTemplate;
import com.example.notification.domain.template.repository.NotificationTemplateRepository;
import com.example.notification.global.exception.BusinessException;
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
class NotificationRequestServiceTest {

    @Mock
    private NotificationRequestRepository requestRepository;
    @Mock
    private NotificationTemplateRepository templateRepository;
    @Mock
    private ReceiverRepository receiverRepository;
    @Mock
    private NotificationRequestEventProducer eventProducer;

    private NotificationRequestService service;

    @BeforeEach
    void setUp() {
        service = new NotificationRequestService(requestRepository, templateRepository, receiverRepository, eventProducer);
    }

    @Test
    void createThrowsWhenRequestKeyDuplicated() {
        NotificationRequestCreateRequest request = request();
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenTemplateMissing() {
        NotificationRequestCreateRequest request = request();
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(false);
        when(templateRepository.findByTemplateCode(request.templateCode())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenReceiverMissing() {
        NotificationRequestCreateRequest request = request();
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(false);
        NotificationTemplate template = new NotificationTemplate();
        when(templateRepository.findByTemplateCode(request.templateCode())).thenReturn(Optional.of(template));
        when(receiverRepository.countByIdInAndActiveTrue(request.receiverIds())).thenReturn(1L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createPublishesQueuedEventWhenValid() {
        NotificationRequestCreateRequest request = request();
        NotificationTemplate template = mock(NotificationTemplate.class);
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(false);
        when(templateRepository.findByTemplateCode(request.templateCode())).thenReturn(Optional.of(template));
        when(template.getEventType()).thenReturn("ORDER");
        when(receiverRepository.countByIdInAndActiveTrue(request.receiverIds())).thenReturn(2L);

        service.create(request);

        ArgumentCaptor<NotificationRequestQueuedEvent> captor = ArgumentCaptor.forClass(NotificationRequestQueuedEvent.class);
        verify(eventProducer).publish(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().priority()).isEqualTo("HIGH");
    }

    @Test
    void createNormalizesLowerCasePriorityToUpperCase() {
        NotificationRequestCreateRequest request = new NotificationRequestCreateRequest(
                "order-1234-paid",
                "ORDER_PAID",
                List.of(1001L, 1002L),
                Map.of("orderNo", "1234"),
                "high"
        );
        NotificationTemplate template = mock(NotificationTemplate.class);
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(false);
        when(templateRepository.findByTemplateCode(request.templateCode())).thenReturn(Optional.of(template));
        when(template.getEventType()).thenReturn("ORDER");
        when(receiverRepository.countByIdInAndActiveTrue(request.receiverIds())).thenReturn(2L);

        service.create(request);

        ArgumentCaptor<NotificationRequestQueuedEvent> captor = ArgumentCaptor.forClass(NotificationRequestQueuedEvent.class);
        verify(eventProducer).publish(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().priority()).isEqualTo("HIGH");
    }

    @Test
    void createThrowsWhenPriorityInvalid() {
        NotificationRequestCreateRequest request = new NotificationRequestCreateRequest(
                "order-1234-paid",
                "ORDER_PAID",
                List.of(1001L, 1002L),
                Map.of("orderNo", "1234"),
                "urgent"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(requestRepository, never()).existsByRequestKey(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenReceiverIdInvalid() {
        NotificationRequestCreateRequest request = new NotificationRequestCreateRequest(
                "order-1234-paid",
                "ORDER_PAID",
                List.of(-1L, 1002L),
                Map.of("orderNo", "1234"),
                "HIGH"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(receiverRepository, never()).countByIdInAndActiveTrue(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenReceiverIdsDuplicated() {
        NotificationRequestCreateRequest request = new NotificationRequestCreateRequest(
                "order-1234-paid",
                "ORDER_PAID",
                List.of(1001L, 1001L),
                Map.of("orderNo", "1234"),
                "HIGH"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(receiverRepository, never()).countByIdInAndActiveTrue(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    void listThrowsWhenPageNegative() {
        assertThatThrownBy(() -> service.list(null, -1, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenSizeOutOfRange() {
        assertThatThrownBy(() -> service.list(null, 0, 101))
                .isInstanceOf(BusinessException.class);
    }

    private NotificationRequestCreateRequest request() {
        return new NotificationRequestCreateRequest(
                "order-1234-paid",
                "ORDER_PAID",
                List.of(1001L, 1002L),
                Map.of("orderNo", "1234"),
                "HIGH"
        );
    }
}
