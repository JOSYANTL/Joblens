package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplicationAnalytics;
import com.josyantl.joblens.job.domain.repository.JobApplicationAnalyticsRepository;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationAnalyticsService {
    private final JobApplicationAnalyticsRepository repository;
    private final CurrentUserProvider currentUser;

    @Transactional(readOnly = true)
    public JobApplicationAnalytics getAnalytics() {
        Long userId = currentUser.userId();
        LocalDate today = LocalDate.now();
        LocalDate firstWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(JobApplicationAnalytics.WEEKS - 1);
        LocalDate nextWeek = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        long applied = repository.countReachedStage(userId,
                List.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW_SCHEDULED,
                        ApplicationStatus.OFFERED, ApplicationStatus.REJECTED),
                ApplicationStatus.APPLIED);
        long interviewed = repository.countReachedStage(userId,
                List.of(ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationStatus.OFFERED),
                ApplicationStatus.INTERVIEW_SCHEDULED);
        long offered = repository.countReachedStage(userId,
                List.of(ApplicationStatus.OFFERED), ApplicationStatus.OFFERED);

        return JobApplicationAnalytics.from(today,
                repository.findCreationDates(userId, firstWeek.atStartOfDay(), nextWeek.atStartOfDay()),
                applied, interviewed, offered);
    }
}
