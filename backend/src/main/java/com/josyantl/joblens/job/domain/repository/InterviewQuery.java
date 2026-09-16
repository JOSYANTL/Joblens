package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.InterviewStatus;
import java.time.Instant;

public record InterviewQuery(Long applicationId, InterviewStatus status, Instant from,
                             Instant to, int page, int size) {
    public InterviewQuery {
        if (applicationId != null && applicationId <= 0)
            throw new IllegalArgumentException("Application id must be positive");
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Page must be nonnegative and size between 1 and 100");
        if (from != null && to != null && !from.isBefore(to))
            throw new IllegalArgumentException("from must be before to");
    }
}
