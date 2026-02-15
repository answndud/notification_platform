package com.example.notification.domain.request.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.receiver.repository.ReceiverRepository;
import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.producer.NotificationRequestEventProducer;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.template.repository.NotificationTemplateRepository;
import com.example.notification.global.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(templateRepository.existsByTemplateCode(request.templateCode())).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(eventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenReceiverMissing() {
        NotificationRequestCreateRequest request = request();
        when(requestRepository.existsByRequestKey(request.requestKey())).thenReturn(false);
        when(templateRepository.existsByTemplateCode(request.templateCode())).thenReturn(true);
        when(receiverRepository.countByIdInAndActiveTrue(request.receiverIds())).thenReturn(1L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);

        verify(eventProducer, never()).publish(any());
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
