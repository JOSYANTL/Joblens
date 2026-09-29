package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface JobApplicationAnalyticsRepository {
    List<LocalDateTime> findCreationDates(Long userId, LocalDateTime from, LocalDateTime toExclusive);

    long countReachedStage(Long userId, List<ApplicationStatus> currentStatuses,
                           ApplicationStatus historicalStatus);
}
