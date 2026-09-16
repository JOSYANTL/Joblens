package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.*;
import java.time.Instant;

public record FollowUpTaskResponse(Long id, Long applicationId, String title, String notes,
        Instant dueAt, FollowUpTaskStatus status, Instant createdAt, Instant updatedAt,
        Instant completedAt, long version, boolean overdue) {
    public static FollowUpTaskResponse from(FollowUpTask task, Instant now) {
        return new FollowUpTaskResponse(task.getId(), task.getApplicationId(), task.getTitle(),
                task.getNotes(), task.getDueAt(), task.getStatus(), task.getCreatedAt(),
                task.getUpdatedAt(), task.getCompletedAt(), task.getVersion(), task.isOverdue(now));
    }
}
