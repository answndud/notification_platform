package com.example.notification.domain.task.service;

import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.task.dto.DeliveryTaskListResponse;
import com.example.notification.domain.task.dto.DeliveryTaskResponse;
import com.example.notification.domain.task.dto.DeliveryTaskRetryResponse;
import com.example.notification.global.common.ApiInputNormalizer;
import com.example.notification.global.common.PageableFactory;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeliveryTaskService {

    private final DeliveryTaskRepository deliveryTaskRepository;

    public DeliveryTaskService(DeliveryTaskRepository deliveryTaskRepository) {
        this.deliveryTaskRepository = deliveryTaskRepository;
    }

    public DeliveryTaskListResponse list(Long requestId, String channel, String status, String priority, int page, int size) {
        Pageable pageable = PageableFactory.of(page, size);
        DeliveryTaskStatus taskStatus = parseStatus(status);
        Page<DeliveryTask> tasks = deliveryTaskRepository.findWithFilters(
                taskStatus,
                requestId,
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

    @Transactional
    public DeliveryTaskRetryResponse retry(Long taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DLQ_TASK_NOT_FOUND));
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

    private DeliveryTaskStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return DeliveryTaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

}
