package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.JobApplication;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository {

    JobApplication save(JobApplication application);

    Optional<JobApplication> findById(Long id);

    List<JobApplication> findAll();
}
