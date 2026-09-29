package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.JobApplicationAnalytics;

import java.util.List;

public record JobApplicationAnalyticsResponse(
        List<JobApplicationAnalytics.WeeklyApplications> weeklyApplications,
        JobApplicationAnalytics.Conversion conversion
) {
    public static JobApplicationAnalyticsResponse from(JobApplicationAnalytics analytics) {
        return new JobApplicationAnalyticsResponse(analytics.weeklyApplications(), analytics.conversion());
    }
}
