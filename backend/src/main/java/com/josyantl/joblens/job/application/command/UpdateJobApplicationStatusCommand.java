package com.josyantl.joblens.job.application.command;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

public record UpdateJobApplicationStatusCommand(
        Long id,
        ApplicationStatus status,
        Long version
) {
}
