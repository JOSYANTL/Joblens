package com.josyantl.joblens.job.application.exception;

public class JobApplicationNotFoundException extends RuntimeException {

    public JobApplicationNotFoundException(Long id) {
        super("Job application not found: " + id);
    }
}
