package com.example.notification.domain.request.repository;

import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.entity.NotificationRequestStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    boolean existsByRequestKey(String requestKey);

    Optional<NotificationRequest> findByRequestKey(String requestKey);

    Page<NotificationRequest> findByStatus(NotificationRequestStatus status, Pageable pageable);

    @Query("""
            select r
            from NotificationRequest r
            where (:status is null or r.status = :status)
              and (:priority is null or r.priority = :priority)
              and (:requestKey is null or lower(r.requestKey) like lower(concat('%', :requestKey, '%')) escape '\\')
            order by r.id desc
            """)
    Page<NotificationRequest> findWithFilters(
            @Param("status") NotificationRequestStatus status,
            @Param("priority") String priority,
            @Param("requestKey") String requestKey,
            Pageable pageable
    );
}
