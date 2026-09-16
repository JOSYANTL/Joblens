package com.josyantl.joblens.job.application.exception;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException(Long id) {
        super("Interview " + id + " not found for this application");
    }
}
