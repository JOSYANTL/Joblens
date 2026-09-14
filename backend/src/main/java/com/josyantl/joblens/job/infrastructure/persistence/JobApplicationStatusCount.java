package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

public interface JobApplicationStatusCount {

    ApplicationStatus getStatus();

    long getCount();
}
