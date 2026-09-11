package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationPersistenceMapper {

    public JobApplicationJpaEntity toEntity(JobApplication application) {
        JobApplicationJpaEntity entity = new JobApplicationJpaEntity();
        entity.setId(application.getId());
        entity.setCompany(application.getCompany());
        entity.setPosition(application.getPosition());
        entity.setDescription(application.getDescription());
        entity.setStatus(application.getStatus());
        entity.setCreatedAt(application.getCreatedAt());
        entity.setUpdatedAt(application.getUpdatedAt());
        return entity;
    }

    public JobApplication toDomain(JobApplicationJpaEntity entity) {
        return JobApplication.restore(
                entity.getId(),
                entity.getCompany(),
                entity.getPosition(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
