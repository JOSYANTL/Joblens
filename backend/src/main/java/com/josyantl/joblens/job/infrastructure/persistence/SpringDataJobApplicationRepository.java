package com.josyantl.joblens.job.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJobApplicationRepository
        extends JpaRepository<JobApplicationJpaEntity, Long> {
}
