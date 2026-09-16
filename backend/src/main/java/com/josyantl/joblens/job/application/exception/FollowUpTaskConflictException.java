package com.josyantl.joblens.job.application.exception;

public class FollowUpTaskConflictException extends RuntimeException {
    public FollowUpTaskConflictException() {
        super("The follow-up task has changed; reload it and use its latest version");
    }
}
