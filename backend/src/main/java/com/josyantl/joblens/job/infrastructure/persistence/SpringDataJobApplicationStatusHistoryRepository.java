package com.josyantl.joblens.job.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJobApplicationStatusHistoryRepository
        extends JpaRepository<JobApplicationStatusHistoryJpaEntity, Long> {

    List<JobApplicationStatusHistoryJpaEntity>
    findByJobApplicationIdOrderByChangedAtAscIdAsc(Long jobApplicationId);
}
