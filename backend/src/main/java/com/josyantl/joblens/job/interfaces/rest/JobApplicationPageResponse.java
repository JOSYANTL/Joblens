package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.repository.JobApplicationPage;

import java.util.List;

public record JobApplicationPageResponse(
        List<JobApplicationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static JobApplicationPageResponse from(JobApplicationPage result) {
        return new JobApplicationPageResponse(
                result.content().stream().map(JobApplicationResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
