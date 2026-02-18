package com.example.notification.worker.domain.policy.repository;

import com.example.notification.worker.domain.policy.entity.ChannelPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelPolicyRepository extends JpaRepository<ChannelPolicy, Long> {

    Optional<ChannelPolicy> findFirstByEventTypeAndChannel(String eventType, String channel);
}
