package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

public record JobApplicationSearchCriteria(
        String keyword,
        ApplicationStatus status,
        int page,
        int size,
        JobApplicationSortField sortBy,
        SortDirection direction
) {
    public JobApplicationSearchCriteria {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (sortBy == null || direction == null) {
            throw new IllegalArgumentException("Sort field and direction are required");
        }
        keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}
