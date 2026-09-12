package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateJobApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {
}
