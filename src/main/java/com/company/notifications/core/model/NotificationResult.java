package com.company.notifications.core.model;

import java.time.Instant;
import java.util.Optional;

/**
 * Result of a notification send operation.
 *
 * Design Decision: Sealed class with Success/Failure variants (Java 17+)
 * - Type-safe result handling without checked exceptions
 * - Forces explicit handling of both success and failure cases
 * - Enables pattern matching for clean code
 * - Immutable and thread-safe
 *
 * This is a functional approach to error handling that:
 * - Makes errors explicit in the return type
 * - Avoids exception overhead for expected failures
 * - Provides rich error information when things go wrong
 */
public sealed interface NotificationResult permits
        NotificationResult.Success,
        NotificationResult.Failure {

    boolean isSuccess();
    boolean isFailure();
    NotificationStatus getStatus();
    Instant getTimestamp();

    record Success(
            String notificationId,
            String providerMessageId,
            NotificationChannel channel,
            String recipient,
            NotificationStatus status,
            Instant timestamp,
            String message
    ) implements NotificationResult {

        public Success {
            if (notificationId == null || notificationId.isBlank()) {
                throw new IllegalArgumentException("notificationId cannot be null or blank");
            }
            if (channel == null) {
                throw new IllegalArgumentException("channel cannot be null");
            }
            if (status == null) {
                status = NotificationStatus.SENT;
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public NotificationStatus getStatus() {
            return status;
        }

        @Override
        public Instant getTimestamp() {
            return timestamp;
        }
    }

    record Failure(
            String notificationId,
            NotificationChannel channel,
            String recipient,
            String errorCode,
            String errorMessage,
            Throwable cause,
            NotificationStatus status,
            Instant timestamp,
            boolean retryable
    ) implements NotificationResult {

        public Failure {
            if (channel == null) {
                throw new IllegalArgumentException("channel cannot be null");
            }
            if (errorMessage == null || errorMessage.isBlank()) {
                throw new IllegalArgumentException("errorMessage cannot be null or blank");
            }
            if (status == null) {
                status = NotificationStatus.FAILED;
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public NotificationStatus getStatus() {
            return status;
        }

        @Override
        public Instant getTimestamp() {
            return timestamp;
        }

        public Optional<Throwable> getCause() {
            return Optional.ofNullable(cause);
        }
    }

    // Factory methods for convenient result creation

    static Success success(
            String notificationId,
            String providerMessageId,
            NotificationChannel channel,
            String recipient
    ) {
        return new Success(
                notificationId,
                providerMessageId,
                channel,
                recipient,
                NotificationStatus.SENT,
                Instant.now(),
                "Notification sent successfully"
        );
    }
    static Success success(
            String notificationId,
            String providerMessageId,
            NotificationChannel channel,
            String recipient,
            String message
    ) {
        return new Success(
                notificationId,
                providerMessageId,
                channel,
                recipient,
                NotificationStatus.SENT,
                Instant.now(),
                message
        );
    }
    static Success queued(
            String notificationId,
            NotificationChannel channel,
            String recipient
    ) {
        return new Success(
                notificationId,
                null,
                channel,
                recipient,
                NotificationStatus.QUEUED,
                Instant.now(),
                "Notification queued for sending"
        );
    }

    static Failure failure(
            String notificationId,
            NotificationChannel channel,
            String recipient,
            String errorCode,
            String errorMessage
    ) {
        return new Failure(
                notificationId,
                channel,
                recipient,
                errorCode,
                errorMessage,
                null,
                NotificationStatus.FAILED,
                Instant.now(),
                false
        );
    }

    static Failure failure(
            String notificationId,
            NotificationChannel channel,
            String recipient,
            String errorCode,
            String errorMessage,
            Throwable cause
    ) {
        return new Failure(
                notificationId,
                channel,
                recipient,
                errorCode,
                errorMessage,
                cause,
                NotificationStatus.FAILED,
                Instant.now(),
                false
        );
    }

    static Failure retryableFailure(
            String notificationId,
            NotificationChannel channel,
            String recipient,
            String errorCode,
            String errorMessage,
            Throwable cause
    ) {
        return new Failure(
                notificationId,
                channel,
                recipient,
                errorCode,
                errorMessage,
                cause,
                NotificationStatus.RETRYING,
                Instant.now(),
                true
        );
    }
}

