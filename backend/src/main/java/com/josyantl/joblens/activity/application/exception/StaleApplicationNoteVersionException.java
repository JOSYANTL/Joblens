package com.josyantl.joblens.activity.application.exception;

public class StaleApplicationNoteVersionException extends RuntimeException {
    public StaleApplicationNoteVersionException() {
        super("The note was changed by another request; reload it and try again");
    }
}
