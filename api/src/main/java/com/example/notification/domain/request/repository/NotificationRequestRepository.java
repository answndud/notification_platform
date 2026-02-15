package com.example.notification.domain.request.repository;

import com.example.notification.domain.request.entity.NotificationRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    boolean existsByRequestKey(String requestKey);

    Optional<NotificationRequest> findByRequestKey(String requestKey);
}
