package com.example.notification.domain.task.service;

import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.domain.task.dto.DeliveryTaskListResponse;
import com.example.notification.domain.task.dto.DeliveryTaskResponse;
import com.example.notification.domain.task.dto.DeliveryTaskRetryResponse;
import com.example.notification.global.common.ApiInputNormalizer;
import com.example.notification.global.common.PageableFactory;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeliveryTaskService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final NotificationRequestRepository notificationRequestRepository;

    public DeliveryTaskService(
            DeliveryTaskRepository deliveryTaskRepository,
            NotificationRequestRepository notificationRequestRepository
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.notificationRequestRepository = notificationRequestRepository;
    }

    public DeliveryTaskListResponse list(
            Long requestId,
            String requestKey,
            String channel,
            String status,
            String priority,
            int page,
            int size
    ) {
        Pageable pageable = PageableFactory.of(page, size);
        DeliveryTaskStatus taskStatus = ApiInputNormalizer.normalizeOptionalEnum(status, DeliveryTaskStatus.class);
        Long resolvedRequestId = resolveRequestId(requestId, requestKey);
        if (Objects.equals(resolvedRequestId, -1L)) {
            return emptyResponse(pageable);
        }
        Page<DeliveryTask> tasks = deliveryTaskRepository.findWithFilters(
                taskStatus,
                resolvedRequestId,
                ApiInputNormalizer.normalizeOptionalChannel(channel),
                ApiInputNormalizer.normalizeOptionalPriority(priority),
                pageable
        );

        return new DeliveryTaskListResponse(
                tasks.getContent().stream().map(this::toResponse).toList(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages()
        );
    }

    private Long resolveRequestId(Long requestId, String requestKey) {
        String normalizedRequestKey = ApiInputNormalizer.normalizeOptionalRequestKey(requestKey);
        if (normalizedRequestKey == null) {
            return requestId;
        }
        var request = notificationRequestRepository.findByRequestKey(normalizedRequestKey);
        if (request.isEmpty()) {
            return -1L;
        }
        Long requestIdByKey = request.get().getId();
        if (requestId != null && !requestId.equals(requestIdByKey)) {
            return -1L;
        }
        return requestIdByKey;
    }

    private DeliveryTaskListResponse emptyResponse(Pageable pageable) {
        return new DeliveryTaskListResponse(
                java.util.List.of(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                0,
                0
        );
    }

    public DeliveryTaskResponse get(Long taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        return toResponse(task);
    }

    @Transactional
    public DeliveryTaskRetryResponse retry(Long taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        task.retryNow();
        deliveryTaskRepository.save(task);

        return new DeliveryTaskRetryResponse(
                task.getId(),
                task.getStatus().name(),
                task.getRetryCount(),
                task.getNextRetryAt()
        );
    }

    private DeliveryTaskResponse toResponse(DeliveryTask task) {
        return new DeliveryTaskResponse(
                task.getId(),
                task.getRequestId(),
                task.getReceiverId(),
                task.getChannel(),
                task.getPriority(),
                task.getStatus().name(),
                task.getRetryCount(),
                task.getMaxRetry(),
                task.getNextRetryAt(),
                task.getCreatedAt()
        );
    }

}
