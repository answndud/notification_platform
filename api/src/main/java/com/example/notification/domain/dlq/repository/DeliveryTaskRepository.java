package com.example.notification.domain.dlq.repository;

import com.example.notification.domain.dlq.entity.DeliveryTask;
import com.example.notification.domain.dlq.entity.DeliveryTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {

    long countByStatus(DeliveryTaskStatus status);

    @Query("""
            select t
            from DeliveryTask t
            where (:status is null or t.status = :status)
              and (:requestId is null or t.requestId = :requestId)
              and (:channel is null or t.channel = :channel)
              and (:priority is null or t.priority = :priority)
            order by t.id desc
            """)
    Page<DeliveryTask> findWithFilters(
            @Param("status") DeliveryTaskStatus status,
            @Param("requestId") Long requestId,
            @Param("channel") String channel,
            @Param("priority") String priority,
            Pageable pageable
    );
}
