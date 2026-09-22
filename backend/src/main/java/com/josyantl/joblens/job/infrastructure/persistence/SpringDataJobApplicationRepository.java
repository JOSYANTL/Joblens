package com.josyantl.joblens.job.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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
}
