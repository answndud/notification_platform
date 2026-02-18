package com.example.notification.domain.request.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.receiver.repository.ReceiverRepository;
import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.entity.NotificationRequestStatus;
import com.example.notification.domain.request.producer.NotificationRequestEventProducer;
import com.example.notification.domain.request.producer.NotificationRequestQueuedEvent;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.template.entity.NotificationTemplate;
import com.example.notification.domain.template.repository.NotificationTemplateRepository;
import com.example.notification.global.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

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
    void createTrimsRequestKeyAndTemplateCode() {
        NotificationRequestCreateRequest request = new NotificationRequestCreateRequest(
                "  order-1234-paid  ",
                "  ORDER_PAID  ",
                List.of(1001L, 1002L),
                Map.of("orderNo", "1234"),
                "HIGH"
        );
        NotificationTemplate template = mock(NotificationTemplate.class);
        when(requestRepository.existsByRequestKey("order-1234-paid")).thenReturn(false);
        when(templateRepository.findByTemplateCode("ORDER_PAID")).thenReturn(Optional.of(template));
        when(template.getEventType()).thenReturn("ORDER");
        when(receiverRepository.countByIdInAndActiveTrue(request.receiverIds())).thenReturn(2L);

        service.create(request);

        verify(requestRepository).existsByRequestKey("order-1234-paid");
        verify(templateRepository).findByTemplateCode("ORDER_PAID");
        ArgumentCaptor<NotificationRequestQueuedEvent> captor = ArgumentCaptor.forClass(NotificationRequestQueuedEvent.class);
        verify(eventProducer).publish(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().requestKey()).isEqualTo("order-1234-paid");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().templateCode()).isEqualTo("ORDER_PAID");
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
        assertThatThrownBy(() -> service.list(null, null, null, -1, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenSizeOutOfRange() {
        assertThatThrownBy(() -> service.list(null, null, null, 0, 101))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenPriorityInvalid() {
        assertThatThrownBy(() -> service.list(null, "urgent", null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listThrowsWhenRequestKeyTooLong() {
        assertThatThrownBy(() -> service.list(null, null, "a".repeat(121), 0, 20))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listMapsPriorityInResponse() {
        NotificationRequest request = mock(NotificationRequest.class);
        when(request.getId()).thenReturn(1L);
        when(request.getRequestKey()).thenReturn("order-1");
        when(request.getPriority()).thenReturn("LOW");
        when(request.getStatus()).thenReturn(NotificationRequestStatus.QUEUED);
        when(request.getRequestedAt()).thenReturn(java.time.LocalDateTime.of(2026, 2, 18, 12, 0));

        when(requestRepository.findWithFilters(NotificationRequestStatus.QUEUED, "LOW", "order", PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(request), PageRequest.of(0, 20), 1));

        var response = service.list("queued", "low", "order", 0, 20);

        org.assertj.core.api.Assertions.assertThat(response.items()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(response.items().getFirst().priority()).isEqualTo("LOW");
    }

    @Test
    void getByRequestKeyTrimsInput() {
        NotificationRequest request = mock(NotificationRequest.class);
        when(request.getId()).thenReturn(9L);
        when(request.getRequestKey()).thenReturn("order-9");
        when(request.getPriority()).thenReturn("HIGH");
        when(request.getStatus()).thenReturn(NotificationRequestStatus.QUEUED);
        when(request.getRequestedAt()).thenReturn(java.time.LocalDateTime.of(2026, 2, 18, 13, 0));
        when(requestRepository.findByRequestKey("order-9")).thenReturn(Optional.of(request));

        var response = service.getByRequestKey("  order-9  ");

        org.assertj.core.api.Assertions.assertThat(response.requestKey()).isEqualTo("order-9");
        verify(requestRepository).findByRequestKey("order-9");
    }

    @Test
    void getByRequestKeyThrowsWhenMissing() {
        when(requestRepository.findByRequestKey("unknown-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByRequestKey("unknown-key"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getByRequestKeyThrowsWhenBlank() {
        assertThatThrownBy(() -> service.getByRequestKey("   "))
                .isInstanceOf(BusinessException.class);

        verify(requestRepository, never()).findByRequestKey(any());
    }

    @Test
    void listEscapesWildcardCharactersInRequestKeyFilter() {
        when(requestRepository.findWithFilters(
                NotificationRequestStatus.QUEUED,
                "LOW",
                "order\\%\\_1",
                PageRequest.of(0, 20)
        )).thenReturn(Page.empty(PageRequest.of(0, 20)));

        service.list("queued", "low", "order%_1", 0, 20);

        verify(requestRepository).findWithFilters(
                NotificationRequestStatus.QUEUED,
                "LOW",
                "order\\%\\_1",
                PageRequest.of(0, 20)
        );
    }

    @Test
    void listUsesNullRequestKeyFilterWhenBlank() {
        when(requestRepository.findWithFilters(
                null,
                null,
                null,
                PageRequest.of(0, 20)
        )).thenReturn(Page.empty(PageRequest.of(0, 20)));

        service.list(null, null, "   ", 0, 20);

        verify(requestRepository).findWithFilters(
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );
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
