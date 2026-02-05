package com.company.notifications.core.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a validation operation.
 * Contains validation status and any error messages.
 */
@Getter
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
    }

    /**
     * Creates a successful validation result
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyList());
    }

    /**
     * Creates a failed validation result with a single error
     */
    public static ValidationResult invalid(String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return new ValidationResult(false, errors);
    }

    /**
     * Creates a failed validation result with multiple errors
     */
    public static ValidationResult invalid(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("Errors list cannot be null or empty for invalid result");
        }
        return new ValidationResult(false, errors);
    }

    /**
     * Gets all validation errors (unmodifiable)
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets the first error message (if any)
     */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    /**
     * Gets the number of errors
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Combines this validation result with another
     */
    public ValidationResult and(ValidationResult other) {
        if (this.valid && other.valid) {
            return valid();
        }

        List<String> combinedErrors = new ArrayList<>();
        combinedErrors.addAll(this.errors);
        combinedErrors.addAll(other.errors);

        return invalid(combinedErrors);
    }

    /**
     * Checks if there are any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true}";
        }
        return String.format("ValidationResult{valid=false, errors=%s}", errors);
    }
}
