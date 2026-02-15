package com.example.notification.domain.request.service;

import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.dto.NotificationRequestResponse;
import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.producer.NotificationRequestEventProducer;
import com.example.notification.domain.request.producer.NotificationRequestQueuedEvent;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.receiver.repository.ReceiverRepository;
import com.example.notification.domain.template.repository.NotificationTemplateRepository;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationRequestService {

    private final NotificationRequestRepository requestRepository;
    private final NotificationTemplateRepository templateRepository;
    private final ReceiverRepository receiverRepository;
    private final NotificationRequestEventProducer eventProducer;

    public NotificationRequestService(NotificationRequestRepository requestRepository,
                                      NotificationTemplateRepository templateRepository,
                                      ReceiverRepository receiverRepository,
                                      NotificationRequestEventProducer eventProducer) {
        this.requestRepository = requestRepository;
        this.templateRepository = templateRepository;
        this.receiverRepository = receiverRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public NotificationRequestResponse create(NotificationRequestCreateRequest command) {
        if (requestRepository.existsByRequestKey(command.requestKey())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST_KEY);
        }
        if (!templateRepository.existsByTemplateCode(command.templateCode())) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        long activeReceiverCount = receiverRepository.countByIdInAndActiveTrue(command.receiverIds());
        if (activeReceiverCount != new HashSet<>(command.receiverIds()).size()) {
            throw new BusinessException(ErrorCode.RECEIVER_NOT_FOUND);
        }

        NotificationRequest request = NotificationRequest.create(
                command.requestKey(),
                command.templateCode(),
                command.priority()
        );
        requestRepository.save(request);

        eventProducer.publish(new NotificationRequestQueuedEvent(
                request.getId(),
                request.getRequestKey(),
                request.getTemplateCode(),
                request.getPriority(),
                command.receiverIds(),
                command.variables()
        ));

        request.markQueued();
        return NotificationRequestResponse.from(request);
    }

    public NotificationRequestResponse get(Long requestId) {
        NotificationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));
        return NotificationRequestResponse.from(request);
    }
}
