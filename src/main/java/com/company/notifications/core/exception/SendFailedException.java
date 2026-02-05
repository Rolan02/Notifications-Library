package com.company.notifications.core.exception;

public class SendFailedException extends RuntimeException {
    public SendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
