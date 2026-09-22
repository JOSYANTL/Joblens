package com.josyantl.joblens.notification.application.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("Notification not found: " + id);
    }
}
