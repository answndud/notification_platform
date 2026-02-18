package com.example.notification.worker.domain.task.repository;

import com.example.notification.worker.domain.task.entity.DeliveryTask;
import com.example.notification.worker.domain.task.entity.DeliveryTaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {

    Optional<DeliveryTask> findByRequestIdAndReceiverIdAndChannel(Long requestId, Long receiverId, String channel);

    @Query(value = """
            select *
            from delivery_task
            where status = :status
              and next_retry_at <= :retryAt
            order by id asc
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<DeliveryTask> findRetryTargetsForUpdate(
            @Param("status") String status,
            @Param("retryAt") LocalDateTime retryAt,
            @Param("batchSize") int batchSize
    );

    List<DeliveryTask> findTop50ByStatusAndNextRetryAtBeforeOrderByIdAsc(
            DeliveryTaskStatus status,
            LocalDateTime retryAt
    );
}
