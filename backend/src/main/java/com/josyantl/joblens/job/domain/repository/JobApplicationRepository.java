package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JobApplicationRepository {

    JobApplication save(JobApplication application, Long userId);

    Optional<JobApplication> findById(Long id, Long userId);

    List<JobApplication> findAll(Long userId);

    JobApplicationPage search(JobApplicationSearchCriteria criteria, Long userId);

    Map<ApplicationStatus, Long> countByStatus(Long userId);

    void deleteById(Long id, Long userId);
}
