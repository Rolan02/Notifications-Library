package com.company.notifications.core.exception;

/**
 * Base exception for all notification-related errors.
 *
 * Design Decision: RuntimeException instead of checked exception
 * - Modern Java best practice: use unchecked exceptions for business logic errors
 * - Doesn't force callers to handle exceptions they can't recover from
 * - Cleaner API without throws declarations everywhere
 * - Still allows explicit catching when needed
 *
 * We use NotificationResult for expected failures (validation, provider errors)
 * We use exceptions for unexpected/unrecoverable errors (configuration, bugs)
 */
public class NotificationException extends RuntimeException {

    private final String errorCode;

    public NotificationException(String message) {
        super(message);
        this.errorCode = "NOTIFICATION_ERROR";
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "NOTIFICATION_ERROR";
    }

    public NotificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public NotificationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
