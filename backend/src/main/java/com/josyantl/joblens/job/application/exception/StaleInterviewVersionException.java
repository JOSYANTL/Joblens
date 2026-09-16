package com.josyantl.joblens.job.application.exception;

public class StaleInterviewVersionException extends RuntimeException {
    public StaleInterviewVersionException() {
        super("The interview has changed; reload it and use its latest version");
    }
}
