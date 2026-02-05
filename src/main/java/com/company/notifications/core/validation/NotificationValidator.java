package com.company.notifications.core.validation;

import com.company.notifications.core.model.Notification;

public interface NotificationValidator<T extends Notification> {

    /**
     * Validates a notification
     *
     * @param notification The notification to validate
     * @return ValidationResult containing validation status and errors
     */
    ValidationResult validate(T notification);

    /**
     * Sets the next validator in the chain
     *
     * @param next The next validator to execute
     * @return This validator (for method chaining)
     */
    NotificationValidator<T> setNext(NotificationValidator<T> next);

    /**
     * Gets the next validator in the chain
     *
     * @return The next validator, or null if this is the last one
     */
    NotificationValidator<T> getNext();

    /**
     * Validates through the entire chain
     *
     * @param notification The notification to validate
     * @return Combined validation result from all validators in the chain
     */
    default ValidationResult validateChain(T notification) {
        ValidationResult result = validate(notification);

        if (getNext() != null) {
            ValidationResult nextResult = getNext().validateChain(notification);
            result = result.and(nextResult);
        }
        return result;
    }
}
