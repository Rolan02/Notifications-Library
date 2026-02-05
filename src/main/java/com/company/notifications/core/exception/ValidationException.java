package com.company.notifications.core.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when notification validation fails.
 * Contains detailed validation error messages.
 */
public class ValidationException extends NotificationException {

    private final List<String> validationErrors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }

    public ValidationException(String message, List<String> validationErrors) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = new ArrayList<>(validationErrors);
    }

    public ValidationException(List<String> validationErrors) {
        super("VALIDATION_ERROR", buildMessage(validationErrors));
        this.validationErrors = new ArrayList<>(validationErrors);
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    public int getErrorCount() {
        return validationErrors.size();
    }

    private static String buildMessage(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation failed";
        }
        if (errors.size() == 1) {
            return "Validation failed: " + errors.get(0);
        }
        return String.format("Validation failed with %d errors: %s",
                errors.size(),
                String.join("; ", errors));
    }
}
