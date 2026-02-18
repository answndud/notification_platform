package com.example.notification.domain.dlq.service;

import com.example.notification.domain.dlq.dto.DlqListResponse;
import com.example.notification.domain.dlq.dto.DlqReplayResponse;
import com.example.notification.domain.dlq.dto.DlqTaskResponse;
import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryLogRepository;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.domain.request.repository.NotificationRequestRepository;
import com.example.notification.global.common.ApiInputNormalizer;
import com.example.notification.global.common.PageableFactory;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DlqService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final NotificationRequestRepository notificationRequestRepository;

    public DlqService(
            DeliveryTaskRepository deliveryTaskRepository,
            DeliveryLogRepository deliveryLogRepository,
            NotificationRequestRepository notificationRequestRepository
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.notificationRequestRepository = notificationRequestRepository;
    }

    public DlqListResponse getDlqTasks(Long requestId, String requestKey, String channel, String priority, int page, int size) {
        Pageable pageable = PageableFactory.of(page, size);
        Long resolvedRequestId = resolveRequestId(requestId, requestKey);
        if (Objects.equals(resolvedRequestId, -1L)) {
            return emptyResponse(pageable);
        }
        Page<DeliveryTask> tasks = deliveryTaskRepository.findWithFilters(
                DeliveryTaskStatus.DLQ,
                resolvedRequestId,
                ApiInputNormalizer.normalizeOptionalChannel(channel),
                ApiInputNormalizer.normalizeOptionalPriority(priority),
                pageable
        );

        List<DlqTaskResponse> items = tasks.getContent().stream().map(this::toResponse).toList();

        return new DlqListResponse(
                items,
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

    private DlqListResponse emptyResponse(Pageable pageable) {
        return new DlqListResponse(
                List.of(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                0,
                0
        );
    }

    @Transactional
    public DlqReplayResponse replay(Long taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DLQ_TASK_NOT_FOUND));
        task.replayNow();
        deliveryTaskRepository.save(task);

        return new DlqReplayResponse(
                task.getId(),
                task.getStatus().name(),
                task.getRetryCount(),
                task.getNextRetryAt()
        );
    }

    public DlqTaskResponse get(Long taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DLQ_TASK_NOT_FOUND));
        if (task.getStatus() != DeliveryTaskStatus.DLQ) {
            throw new BusinessException(ErrorCode.DLQ_TASK_NOT_FOUND);
        }
        return toResponse(task);
    }

    private DlqTaskResponse toResponse(DeliveryTask task) {
        var latestLog = deliveryLogRepository.findTopByTaskIdOrderByLoggedAtDescIdDesc(task.getId()).orElse(null);
        return new DlqTaskResponse(
                task.getId(),
                task.getRequestId(),
                task.getReceiverId(),
                task.getChannel(),
                task.getPriority(),
                task.getStatus().name(),
                task.getRetryCount(),
                task.getMaxRetry(),
                task.getNextRetryAt(),
                latestLog == null ? null : latestLog.getResultCode(),
                latestLog == null ? null : latestLog.getResultMessage(),
                task.getCreatedAt()
        );
    }

}
