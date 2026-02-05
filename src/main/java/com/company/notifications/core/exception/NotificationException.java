package com.company.notifications.core.exception;

public class NotificationException extends RuntimeException {

    protected NotificationException(String message) {
        super(message);
    }
    protected NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
