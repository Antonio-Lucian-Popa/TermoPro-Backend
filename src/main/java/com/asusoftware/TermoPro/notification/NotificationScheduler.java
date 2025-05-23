package com.asusoftware.TermoPro.notification;

import com.asusoftware.TermoPro.notification.model.NotificationType;
import com.asusoftware.TermoPro.notification.service.NotificationService;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import com.asusoftware.TermoPro.user_time_off.model.UserTimeOff;
import com.asusoftware.TermoPro.user_time_off.repository.UserTimeOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final UserTimeOffRepository timeOffRepository;
    private final NotificationService notificationService;

    // Zilnic la ora 08:00 - notifică OWNER-ul dacă există cereri de concediu recente
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyOwnerAboutTimeOff() {
        List<UserTimeOff> upcomingTimeOff = timeOffRepository.findAllByStartDateBetween(LocalDate.now(), LocalDate.now().plusDays(3));

        upcomingTimeOff.forEach(timeOff -> {
            UUID userId = timeOff.getUserId();
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                User owner = userRepository.findOwnerByCompanyId(user.getCompanyId());

                if (owner != null) {
                    String msg = String.format("%s %s are concediu începând cu %s.",
                            user.getFirstName(),
                            user.getLastName(),
                            timeOff.getStartDate()
                    );
                    notificationService.notifyUser(owner.getId(), "Concediu angajat", msg, NotificationType.INFO);
                }
            }
        });

    }
}