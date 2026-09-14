package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;

import java.util.List;

public interface JobApplicationStatusHistoryRepository {

    JobApplicationStatusHistory save(JobApplicationStatusHistory history);

    List<JobApplicationStatusHistory> findByJobApplicationId(Long jobApplicationId);
}
