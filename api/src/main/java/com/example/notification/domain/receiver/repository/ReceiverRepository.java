package com.example.notification.domain.receiver.repository;

import com.example.notification.domain.receiver.entity.Receiver;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {

    long countByIdInAndActiveTrue(List<Long> ids);
}
