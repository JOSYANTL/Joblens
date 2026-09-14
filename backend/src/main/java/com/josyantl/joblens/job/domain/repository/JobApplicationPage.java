package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.JobApplication;

import java.util.List;

public record JobApplicationPage(
        List<JobApplication> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
