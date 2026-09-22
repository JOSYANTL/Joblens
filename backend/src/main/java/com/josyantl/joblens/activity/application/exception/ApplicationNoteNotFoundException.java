package com.josyantl.joblens.activity.application.exception;

public class ApplicationNoteNotFoundException extends RuntimeException {
    public ApplicationNoteNotFoundException(Long id) {
        super("Application note " + id + " was not found");
    }
}
