package com.company.notifications.core.validation;

import com.company.notifications.core.model.EmailNotification;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for email notifications.
 * Validates email addresses, subject, and other email-specific fields.
 */
public class EmailNotificationValidator extends AbstractValidator<EmailNotification> {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();
    private static final int MAX_SUBJECT_LENGTH = 998; // RFC 2822 limit
    private static final int MAX_BODY_LENGTH = 100_000; // Reasonable limit

    @Override
    public ValidationResult validate(EmailNotification notification) {
        List<String> errors = new ArrayList<>();

        // Basic field validation
        ValidationResult basicValidation = validateBasicFields(notification);
        if (!basicValidation.isValid()) {
            errors.addAll(basicValidation.getErrors());
        }

        // Email-specific validation
        if (notification.getRecipient() != null && !notification.getRecipient().isBlank()) {
            if (!EMAIL_VALIDATOR.isValid(notification.getRecipient())) {
                errors.add("Invalid recipient email address: " + notification.getRecipient());
            }
        }

        // Subject validation
        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            errors.add("Email subject cannot be null or empty");
        } else if (notification.getSubject().length() > MAX_SUBJECT_LENGTH) {
            errors.add("Email subject exceeds maximum length of " + MAX_SUBJECT_LENGTH + " characters");
        }

        // Body length validation
        if (notification.getContent() != null && notification.getContent().getBody() != null) {
            if (notification.getContent().getBody().length() > MAX_BODY_LENGTH) {
                errors.add("Email body exceeds maximum length of " + MAX_BODY_LENGTH + " characters");
            }
        }

        // Validate CC addresses
        if (notification.getCc() != null) {
            for (String cc : notification.getCc()) {
                if (!EMAIL_VALIDATOR.isValid(cc)) {
                    errors.add("Invalid CC email address: " + cc);
                }
            }
        }

        // Validate BCC addresses
        if (notification.getBcc() != null) {
            for (String bcc : notification.getBcc()) {
                if (!EMAIL_VALIDATOR.isValid(bcc)) {
                    errors.add("Invalid BCC email address: " + bcc);
                }
            }
        }

        // Validate from email (if provided)
        if (notification.getFromEmail() != null && !notification.getFromEmail().isBlank()) {
            if (!EMAIL_VALIDATOR.isValid(notification.getFromEmail())) {
                errors.add("Invalid from email address: " + notification.getFromEmail());
            }
        }

        // Validate reply-to email (if provided)
        if (notification.getReplyTo() != null && !notification.getReplyTo().isBlank()) {
            if (!EMAIL_VALIDATOR.isValid(notification.getReplyTo())) {
                errors.add("Invalid reply-to email address: " + notification.getReplyTo());
            }
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
}
