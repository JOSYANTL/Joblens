package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;

import java.time.LocalDateTime;

public record JobApplicationStatusHistoryResponse(
        Long id,
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        LocalDateTime changedAt
) {
    public static JobApplicationStatusHistoryResponse from(
            JobApplicationStatusHistory history
    ) {
        return new JobApplicationStatusHistoryResponse(
                history.id(),
                history.fromStatus(),
                history.toStatus(),
                history.changedAt()
        );
    }
}
