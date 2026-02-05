package com.company.notifications.core.validation;

import com.company.notifications.core.model.Notification;

/**
 * Abstract base validator implementing the chain of responsibility pattern.
 * Concrete validators should extend this class.
 */
public abstract class AbstractValidator<T extends Notification> implements NotificationValidator<T> {

    private NotificationValidator<T> next;

    @Override
    public NotificationValidator<T> setNext(NotificationValidator<T> next) {
        this.next = next;
        return this;
    }

    @Override
    public NotificationValidator<T> getNext() {
        return next;
    }

    /**
     * Validates basic notification fields that are common to all notifications
     */
    protected ValidationResult validateBasicFields(Notification notification) {
        if (notification == null) {
            return ValidationResult.invalid("Notification cannot be null");
        }

        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            return ValidationResult.invalid("Recipient cannot be null or empty");
        }

        if (notification.getContent() == null) {
            return ValidationResult.invalid("Notification content cannot be null");
        }

        if (notification.getContent().getBody() == null || notification.getContent().getBody().isBlank()) {
            return ValidationResult.invalid("Notification body cannot be null or empty");
        }

        return ValidationResult.valid();
    }
}
