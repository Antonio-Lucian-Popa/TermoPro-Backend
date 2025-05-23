package com.asusoftware.TermoPro.notification.controller;

import com.asusoftware.TermoPro.notification.model.Notification;
import com.asusoftware.TermoPro.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable UUID userId) {
        return notificationRepository.findAllByUserIdOrderByTimestampDesc(userId);
    }

    @GetMapping("/company/{companyId}")
    public List<Notification> getCompanyNotifications(@PathVariable UUID companyId) {
        return notificationRepository.findAllByCompanyIdAndUserIdIsNullOrderByTimestampDesc(companyId);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }
}
