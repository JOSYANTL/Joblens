package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.repository.JobApplicationAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaJobApplicationAnalyticsRepository implements JobApplicationAnalyticsRepository {
    private final SpringDataJobApplicationRepository repository;

    @Override
    public List<LocalDateTime> findCreationDates(Long userId, LocalDateTime from,
                                                 LocalDateTime toExclusive) {
        return repository.findCreationDates(userId, from, toExclusive);
    }

    @Override
    public long countReachedStage(Long userId, List<ApplicationStatus> currentStatuses,
                                  ApplicationStatus historicalStatus) {
        return repository.countReachedStage(userId, currentStatuses, historicalStatus);
    }
}
