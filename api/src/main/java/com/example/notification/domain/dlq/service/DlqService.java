package com.example.notification.domain.dlq.service;

import com.example.notification.domain.dlq.dto.DlqListResponse;
import com.example.notification.domain.dlq.dto.DlqReplayResponse;
import com.example.notification.domain.dlq.dto.DlqTaskResponse;
import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import com.example.notification.domain.dlq.repository.DeliveryLogRepository;
import com.example.notification.domain.dlq.repository.DeliveryTaskRepository;
import com.example.notification.global.common.PageableFactory;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DlqService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryLogRepository deliveryLogRepository;

    public DlqService(DeliveryTaskRepository deliveryTaskRepository, DeliveryLogRepository deliveryLogRepository) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryLogRepository = deliveryLogRepository;
    }

    public DlqListResponse getDlqTasks(Long requestId, String channel, String priority, int page, int size) {
        Pageable pageable = PageableFactory.of(page, size);
        Page<DeliveryTask> tasks = deliveryTaskRepository.findWithFilters(
                DeliveryTaskStatus.DLQ,
                requestId,
                normalizeChannel(channel),
                normalizePriority(priority),
                pageable
        );

        List<DlqTaskResponse> items = tasks.getContent().stream()
                .map(task -> {
                    var latestLog = deliveryLogRepository.findTopByTaskIdOrderByLoggedAtDescIdDesc(task.getId()).orElse(null);
                    return new DlqTaskResponse(
                            task.getId(),
                            task.getRequestId(),
                            task.getReceiverId(),
                            task.getChannel(),
                            task.getPriority(),
                            task.getRetryCount(),
                            task.getMaxRetry(),
                            latestLog == null ? null : latestLog.getResultCode(),
                            latestLog == null ? null : latestLog.getResultMessage(),
                            task.getCreatedAt()
                    );
                })
                .toList();

        return new DlqListResponse(
                items,
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages()
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

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return null;
        }
        return channel.trim().toUpperCase();
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        String normalized = priority.trim().toUpperCase();
        return switch (normalized) {
            case "HIGH", "NORMAL", "LOW" -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}
