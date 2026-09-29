package com.josyantl.joblens.notification.application.service;

import com.josyantl.joblens.notification.domain.model.*;
import com.josyantl.joblens.notification.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationReminderService {
    private static final Duration REMINDER_WINDOW = Duration.ofHours(24);
    private final NotificationRepository notifications;
    private final ReminderCandidateRepository candidates;

    @Transactional
    public int generate(Instant now) {
        Instant until = now.plus(REMINDER_WINDOW);
        int created = 0;
        for (ReminderCandidate candidate : candidates.findUpcomingInterviews(now, until)) {
            created += create(candidate, NotificationType.INTERVIEW_UPCOMING,
                    NotificationSourceType.INTERVIEW, "面试即将开始",
                    candidate.company() + "的" + candidate.label() + "将在 24 小时内开始", now);
        }
        for (ReminderCandidate candidate : candidates.findTasksDueSoon(now, until)) {
            created += create(candidate, NotificationType.TASK_DUE_SOON,
                    NotificationSourceType.TASK, "跟进任务即将到期",
                    candidate.company() + " · " + candidate.label() + "将在 24 小时内到期", now);
        }
        for (ReminderCandidate candidate : candidates.findOverdueTasks(now)) {
            created += create(candidate, NotificationType.TASK_OVERDUE,
                    NotificationSourceType.TASK, "跟进任务已逾期",
                    candidate.company() + " · " + candidate.label() + "已经逾期", now);
        }
        return created;
    }

    private int create(ReminderCandidate candidate, NotificationType type,
            NotificationSourceType sourceType, String title, String message, Instant now) {
        String key = type + ":" + candidate.sourceId() + ":" + candidate.eventAt().toEpochMilli();
        if (notifications.existsByDeduplicationKey(key)) return 0;
        notifications.save(Notification.create(candidate.userId(), candidate.applicationId(), type,
                sourceType, candidate.sourceId(), title, message, candidate.eventAt(), key, now));
        return 1;
    }
}

