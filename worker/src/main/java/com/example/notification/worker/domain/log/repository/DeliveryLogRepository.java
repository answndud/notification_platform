package com.example.notification.worker.domain.log.repository;

import com.example.notification.worker.domain.log.entity.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
}
