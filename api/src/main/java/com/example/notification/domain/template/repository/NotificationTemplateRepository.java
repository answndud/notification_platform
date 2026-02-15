package com.example.notification.domain.template.repository;

import com.example.notification.domain.template.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    boolean existsByTemplateCode(String templateCode);
}
