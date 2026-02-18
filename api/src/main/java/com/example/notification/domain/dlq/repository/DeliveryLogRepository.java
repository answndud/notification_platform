package com.example.notification.domain.dlq.repository;

import com.example.notification.domain.dlq.entity.DeliveryLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {

    Optional<DeliveryLog> findTopByTaskIdOrderByLoggedAtDescIdDesc(Long taskId);

    @Query("select avg(l.latencyMs) from DeliveryLog l where l.latencyMs is not null")
    Double findAverageLatencyMs();
}
