package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

import java.util.Set;

public record AvailableApplicationStatusesResponse(
        ApplicationStatus currentStatus,
        Set<ApplicationStatus> availableStatuses
) {
}
