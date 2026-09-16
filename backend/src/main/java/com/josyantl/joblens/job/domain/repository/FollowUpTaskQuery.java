package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.FollowUpTaskStatus;
import java.time.Instant;

public record FollowUpTaskQuery(Long applicationId, FollowUpTaskStatus status,
        Instant dueFrom, Instant dueTo, boolean overdueOnly, int page, int size) {
    public FollowUpTaskQuery {
        if (applicationId != null && applicationId <= 0)
            throw new IllegalArgumentException("Application id must be positive");
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Page must be nonnegative and size between 1 and 100");
        if (dueFrom != null && dueTo != null && !dueFrom.isBefore(dueTo))
            throw new IllegalArgumentException("dueFrom must be before dueTo");
        if (overdueOnly && status != null && status != FollowUpTaskStatus.TODO)
            throw new IllegalArgumentException("Overdue filter requires TODO status");
    }
}
