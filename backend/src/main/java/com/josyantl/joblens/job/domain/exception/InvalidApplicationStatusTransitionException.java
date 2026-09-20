package com.josyantl.joblens.job.domain.exception;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;

public class InvalidApplicationStatusTransitionException extends RuntimeException {

    public InvalidApplicationStatusTransitionException(
            ApplicationStatus currentStatus,
            ApplicationStatus targetStatus
    ) {
        super("Cannot change application status from "
                + currentStatus + " to " + targetStatus);
    }
}
