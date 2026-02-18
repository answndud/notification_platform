package com.example.notification.domain.request.service;

import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.dto.NotificationRequestListResponse;
import com.example.notification.domain.request.dto.NotificationRequestResponse;
import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.entity.NotificationRequestStatus;
import com.example.notification.domain.request.producer.NotificationRequestEventProducer;
import com.example.notification.domain.request.producer.NotificationRequestQueuedEvent;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.receiver.repository.ReceiverRepository;
import com.example.notification.domain.template.entity.NotificationTemplate;
import com.example.notification.domain.template.repository.NotificationTemplateRepository;
import com.example.notification.global.common.ApiInputNormalizer;
import com.example.notification.global.common.PageableFactory;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        String normalizedPriority = ApiInputNormalizer.normalizeRequiredPriority(command.priority());
        validateReceiverIds(command.receiverIds());

        if (requestRepository.existsByRequestKey(command.requestKey())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST_KEY);
        }
        NotificationTemplate template = templateRepository.findByTemplateCode(command.templateCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
        long activeReceiverCount = receiverRepository.countByIdInAndActiveTrue(command.receiverIds());
        if (activeReceiverCount != command.receiverIds().size()) {
            throw new BusinessException(ErrorCode.RECEIVER_NOT_FOUND);
        }

        NotificationRequest request = NotificationRequest.create(
                command.requestKey(),
                command.templateCode(),
                normalizedPriority
        );
        requestRepository.save(request);

        eventProducer.publish(new NotificationRequestQueuedEvent(
                request.getId(),
                request.getRequestKey(),
                request.getTemplateCode(),
                template.getEventType(),
                normalizedPriority,
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

    public NotificationRequestListResponse list(String status, int page, int size) {
        Pageable pageable = PageableFactory.of(page, size);
        NotificationRequestStatus requestStatus = parseStatus(status);
        Page<NotificationRequest> requests = requestStatus == null
                ? requestRepository.findAll(pageable)
                : requestRepository.findByStatus(requestStatus, pageable);

        return new NotificationRequestListResponse(
                requests.getContent().stream().map(NotificationRequestResponse::from).toList(),
                requests.getNumber(),
                requests.getSize(),
                requests.getTotalElements(),
                requests.getTotalPages()
        );
    }

    private NotificationRequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return NotificationRequestStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateReceiverIds(List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        HashSet<Long> uniqueReceiverIds = new HashSet<>(receiverIds.size());
        for (Long receiverId : receiverIds) {
            if (receiverId == null || receiverId <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            if (!uniqueReceiverIds.add(receiverId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }
    }

}
