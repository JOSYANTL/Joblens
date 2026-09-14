package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplicationStatistics;

import java.util.Map;

public record JobApplicationStatisticsResponse(
        long total,
        Map<ApplicationStatus, Long> byStatus
) {
    public static JobApplicationStatisticsResponse from(
            JobApplicationStatistics statistics
    ) {
        return new JobApplicationStatisticsResponse(
                statistics.total(),
                statistics.byStatus()
        );
    }
}
