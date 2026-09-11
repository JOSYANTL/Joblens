package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.JobApplication;

import java.util.List;

public interface JobApplicationRepository {

    JobApplication save(JobApplication application);

    List<JobApplication> findAll();
}
