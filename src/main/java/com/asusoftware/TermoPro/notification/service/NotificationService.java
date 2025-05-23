package com.asusoftware.TermoPro.notification.service;

import com.asusoftware.TermoPro.notification.model.Notification;
import com.asusoftware.TermoPro.notification.model.NotificationMessage;
import com.asusoftware.TermoPro.notification.model.NotificationType;
import com.asusoftware.TermoPro.notification.repository.NotificationRepository;
import com.asusoftware.TermoPro.team.model.TeamMember;
import com.asusoftware.TermoPro.team.repository.TeamMembersRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TeamMembersRepository teamMembersRepository;

    public void notifyCompany(UUID companyId, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .companyId(companyId)
                .title(title)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        // 1. Salvăm în DB
        notificationRepository.save(notification);

        // 2. Trimitem prin WebSocket
        NotificationMessage wsMessage = new NotificationMessage(
                title,
                message,
                type.name().toLowerCase(),
                notification.getTimestamp()
        );
        sendNotificationToCompany(companyId, wsMessage);
    }

    public void notifyUser(UUID userId, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .companyId(userRepository.findById(userId).map(User::getCompanyId).orElse(null))
                .title(title)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
        notificationRepository.save(notification);

        NotificationMessage wsMessage = new NotificationMessage(
                title,
                message,
                type.name().toLowerCase(),
                notification.getTimestamp()
        );
        messagingTemplate.convertAndSend("/topic/user/" + userId, wsMessage);
    }

    public void notifyTeam(UUID teamId, String title, String message, NotificationType type) {
        List<TeamMember> members = teamMembersRepository.findAllByTeamId(teamId);
        members.forEach(member -> notifyUser(member.getUserId(), title, message, type));
    }

    public void sendNotificationToCompany(UUID companyId, NotificationMessage message) {
        messagingTemplate.convertAndSend("/topic/company/" + companyId, message);
    }
}
