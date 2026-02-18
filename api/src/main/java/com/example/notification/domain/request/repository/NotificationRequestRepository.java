package com.example.notification.domain.request.repository;

import com.example.notification.domain.request.entity.NotificationRequest;
import com.example.notification.domain.request.entity.NotificationRequestStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    boolean existsByRequestKey(String requestKey);

    Optional<NotificationRequest> findByRequestKey(String requestKey);

    Page<NotificationRequest> findByStatus(NotificationRequestStatus status, Pageable pageable);
}
