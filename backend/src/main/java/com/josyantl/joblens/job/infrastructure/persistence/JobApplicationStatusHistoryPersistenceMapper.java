package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationStatusHistoryPersistenceMapper {

    public JobApplicationStatusHistoryJpaEntity toEntity(
            JobApplicationStatusHistory history
    ) {
        JobApplicationStatusHistoryJpaEntity entity =
                new JobApplicationStatusHistoryJpaEntity();
        entity.setId(history.id());
        entity.setJobApplicationId(history.jobApplicationId());
        entity.setFromStatus(history.fromStatus());
        entity.setToStatus(history.toStatus());
        entity.setChangedAt(history.changedAt());
        return entity;
    }

    public JobApplicationStatusHistory toDomain(
            JobApplicationStatusHistoryJpaEntity entity
    ) {
        return new JobApplicationStatusHistory(
                entity.getId(),
                entity.getJobApplicationId(),
                entity.getFromStatus(),
                entity.getToStatus(),
                entity.getChangedAt()
        );
    }
}
