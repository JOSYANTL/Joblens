package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateJobApplicationStatusRequest(
        @NotNull ApplicationStatus status,
        @NotNull @PositiveOrZero Long version
) {
}
