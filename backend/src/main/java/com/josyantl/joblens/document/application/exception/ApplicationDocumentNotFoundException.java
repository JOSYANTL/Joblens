package com.josyantl.joblens.document.application.exception;

public class ApplicationDocumentNotFoundException extends RuntimeException {
    public ApplicationDocumentNotFoundException(Long id) {
        super("Application document " + id + " was not found");
    }
}
