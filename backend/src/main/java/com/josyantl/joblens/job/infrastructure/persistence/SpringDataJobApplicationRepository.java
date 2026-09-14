package com.josyantl.joblens.job.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataJobApplicationRepository
        extends JpaRepository<JobApplicationJpaEntity, Long>,
        JpaSpecificationExecutor<JobApplicationJpaEntity> {

    @Query("""
            SELECT application.status AS status, COUNT(application) AS count
            FROM JobApplicationJpaEntity application
            GROUP BY application.status
            """)
    List<JobApplicationStatusCount> countGroupedByStatus();
}
