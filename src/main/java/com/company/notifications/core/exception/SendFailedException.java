package com.company.notifications.core.exception;

import com.company.notifications.core.model.NotificationChannel;

/**
 * Exception thrown when sending a notification fails.
 * This is typically used for critical failures that should stop execution,
 * while normal send failures should return NotificationResult.Failure.
 */
public class SendFailedException extends NotificationException {

    private final NotificationChannel channel;
    private final String recipient;
    private final boolean retryable;

    public SendFailedException(String message, NotificationChannel channel, String recipient) {
        super("SEND_FAILED", message);
        this.channel = channel;
        this.recipient = recipient;
        this.retryable = false;
    }

    public SendFailedException(String message, NotificationChannel channel, String recipient, Throwable cause) {
        super("SEND_FAILED", message, cause);
        this.channel = channel;
        this.recipient = recipient;
        this.retryable = false;
    }

    public SendFailedException(String message, NotificationChannel channel, String recipient, boolean retryable) {
        super("SEND_FAILED", message);
        this.channel = channel;
        this.recipient = recipient;
        this.retryable = retryable;
    }

    public SendFailedException(String message, NotificationChannel channel, String recipient, Throwable cause, boolean retryable) {
        super("SEND_FAILED", message, cause);
        this.channel = channel;
        this.recipient = recipient;
        this.retryable = retryable;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
