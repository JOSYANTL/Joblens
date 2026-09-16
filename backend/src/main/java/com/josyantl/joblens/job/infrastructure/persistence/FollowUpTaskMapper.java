package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.FollowUpTask;
import org.springframework.stereotype.Component;

@Component
public class FollowUpTaskMapper {
    public FollowUpTask toDomain(FollowUpTaskJpaEntity entity) {
        return FollowUpTask.restore(entity.getId(), entity.getApplicationId(), entity.getTitle(),
                entity.getNotes(), entity.getDueAt(), entity.getStatus(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getCompletedAt(), entity.getVersion());
    }

    public FollowUpTaskJpaEntity toEntity(FollowUpTask task) {
        var entity = new FollowUpTaskJpaEntity();
        entity.setId(task.getId());
        entity.setApplicationId(task.getApplicationId());
        entity.setTitle(task.getTitle());
        entity.setNotes(task.getNotes());
        entity.setDueAt(task.getDueAt());
        entity.setStatus(task.getStatus());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        entity.setCompletedAt(task.getCompletedAt());
        entity.setVersion(task.getId() == null ? null : task.getVersion());
        return entity;
    }
}
