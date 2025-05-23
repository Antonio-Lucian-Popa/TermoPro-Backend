package com.asusoftware.TermoPro.notification.repository;

import com.asusoftware.TermoPro.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByUserIdOrderByTimestampDesc(UUID userId);
    List<Notification> findAllByCompanyIdAndUserIdIsNullOrderByTimestampDesc(UUID companyId);
}
