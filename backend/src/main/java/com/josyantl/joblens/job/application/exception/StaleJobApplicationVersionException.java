package com.josyantl.joblens.job.application.exception;

public class StaleJobApplicationVersionException extends RuntimeException {

    public StaleJobApplicationVersionException(Long id, long expected, long actual) {
        super("Job application " + id + " has version " + actual
                + "; request used stale version " + expected);
    }
}
