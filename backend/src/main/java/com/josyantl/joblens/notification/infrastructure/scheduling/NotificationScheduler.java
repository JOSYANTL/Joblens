package com.josyantl.joblens.notification.infrastructure.scheduling;

import com.josyantl.joblens.notification.application.service.NotificationReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationReminderService reminders;
    private final Clock clock;

    @Scheduled(initialDelayString = "${joblens.notifications.initial-delay:PT1M}",
            fixedDelayString = "${joblens.notifications.interval:PT15M}")
    public void generateReminders() {
        reminders.generate(Instant.now(clock));
    }
}

