package com.josyantl.joblens.job.application.exception;

public class FollowUpTaskNotFoundException extends RuntimeException {
    public FollowUpTaskNotFoundException(Long id) {
        super("Follow-up task " + id + " not found for this application");
    }
}
