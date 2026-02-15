package com.example.notification.worker.domain.task.repository;

import com.example.notification.worker.domain.task.entity.DeliveryTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
}
