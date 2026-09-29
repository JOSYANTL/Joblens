package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface SpringDataJobApplicationRepository
        extends JpaRepository<JobApplicationJpaEntity, Long>,
        JpaSpecificationExecutor<JobApplicationJpaEntity> {

    Optional<JobApplicationJpaEntity> findByIdAndUserId(Long id, Long userId);

    List<JobApplicationJpaEntity> findAllByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT application.status AS status, COUNT(application) AS count
            FROM JobApplicationJpaEntity application
            WHERE application.userId = :userId
            GROUP BY application.status
            """)
    List<JobApplicationStatusCount> countGroupedByStatus(Long userId);

    @Query("""
            SELECT application.createdAt
            FROM JobApplicationJpaEntity application
            WHERE application.userId = :userId
              AND application.createdAt >= :from
              AND application.createdAt < :toExclusive
            """)
    List<LocalDateTime> findCreationDates(Long userId, LocalDateTime from,
                                          LocalDateTime toExclusive);

    @Query("""
            SELECT COUNT(application)
            FROM JobApplicationJpaEntity application
            WHERE application.userId = :userId
              AND (application.status IN :currentStatuses OR EXISTS (
                SELECT history.id FROM JobApplicationStatusHistoryJpaEntity history
                WHERE history.jobApplicationId = application.id
                  AND history.toStatus = :historicalStatus
              ))
            """)
    long countReachedStage(Long userId, List<ApplicationStatus> currentStatuses,
                           ApplicationStatus historicalStatus);
}
