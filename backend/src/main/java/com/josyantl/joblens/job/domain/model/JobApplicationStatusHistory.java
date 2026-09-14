package com.josyantl.joblens.job.domain.model;

import java.time.LocalDateTime;

public record JobApplicationStatusHistory(
        Long id,
        Long jobApplicationId,
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        LocalDateTime changedAt
) {
    public JobApplicationStatusHistory {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
        if (jobApplicationId == null || jobApplicationId <= 0) {
            throw new IllegalArgumentException("Job application id must be positive");
        }
        if (toStatus == null) {
            throw new IllegalArgumentException("Target status must not be null");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Changed at must not be null");
        }
    }

    public static JobApplicationStatusHistory creation(
            Long jobApplicationId,
            ApplicationStatus initialStatus,
            LocalDateTime changedAt
    ) {
        return new JobApplicationStatusHistory(
                null,
                jobApplicationId,
                null,
                initialStatus,
                changedAt
        );
    }

    public static JobApplicationStatusHistory change(
            Long jobApplicationId,
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            LocalDateTime changedAt
    ) {
        if (fromStatus == null) {
            throw new IllegalArgumentException("Previous status must not be null");
        }
        return new JobApplicationStatusHistory(
                null,
                jobApplicationId,
                fromStatus,
                toStatus,
                changedAt
        );
    }
}
