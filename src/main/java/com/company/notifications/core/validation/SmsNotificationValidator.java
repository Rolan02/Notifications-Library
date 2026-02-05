package com.company.notifications.core.validation;

import com.company.notifications.core.model.SmsNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for SMS notifications.
 * Validates phone numbers and SMS-specific constraints.
 */
public class SmsNotificationValidator extends AbstractValidator<SmsNotification> {

    // E.164 format: + followed by 1-15 digits
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    // More lenient pattern for other formats (allows spaces, dashes, parentheses)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$");

    // SMS length limits
    private static final int MAX_SMS_LENGTH_GSM7 = 160;  // GSM-7 encoding
    private static final int MAX_SMS_LENGTH_UCS2 = 70;   // Unicode (UCS-2) encoding
    private static final int MAX_CONCATENATED_SMS = 1600; // ~10 messages

    @Override
    public ValidationResult validate(SmsNotification notification) {
        List<String> errors = new ArrayList<>();

        // Basic field validation
        ValidationResult basicValidation = validateBasicFields(notification);
        if (!basicValidation.isValid()) {
            errors.addAll(basicValidation.getErrors());
        }

        // Phone number validation
        if (notification.getRecipient() != null && !notification.getRecipient().isBlank()) {
            String phoneNumber = notification.getRecipient().trim();

            // Try E.164 format first (most strict, recommended for international SMS)
            if (!E164_PATTERN.matcher(phoneNumber).matches()) {
                // Try lenient format
                if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                    errors.add("Invalid phone number format: " + phoneNumber +
                            ". Expected E.164 format (e.g., +1234567890) or valid phone number");
                } else {
                    // Valid but not E.164, add warning in metadata
                    errors.add("Phone number is not in E.164 format (recommended): " + phoneNumber);
                }
            }
        }

        // Message length validation
        if (notification.getContent() != null && notification.getContent().getBody() != null) {
            String body = notification.getContent().getBody();
            int length = body.length();

            // Check if message contains non-GSM characters
            boolean hasUnicode = !isGSM7(body);
            int maxLength = hasUnicode ? MAX_SMS_LENGTH_UCS2 : MAX_SMS_LENGTH_GSM7;

            if (length > MAX_CONCATENATED_SMS) {
                errors.add(String.format("SMS body exceeds maximum length of %d characters (current: %d)",
                        MAX_CONCATENATED_SMS, length));
            } else if (length > maxLength) {
                int segments = (int) Math.ceil((double) length / maxLength);
                // This is a warning, not an error - concatenated SMS is allowed
                String warning = String.format(
                        "SMS body exceeds single message limit (%d chars). Will be sent as %d segments. " +
                                "Encoding: %s, Length: %d",
                        maxLength, segments, hasUnicode ? "Unicode" : "GSM-7", length
                );
                // We don't add this as an error, just log it
                // In a real implementation, you might add this to metadata
            }
        }

        // Validate from phone number (if provided)
        if (notification.getFromPhoneNumber() != null && !notification.getFromPhoneNumber().isBlank()) {
            String fromNumber = notification.getFromPhoneNumber().trim();
            if (!E164_PATTERN.matcher(fromNumber).matches() && !PHONE_PATTERN.matcher(fromNumber).matches()) {
                errors.add("Invalid from phone number format: " + fromNumber);
            }
        }

        // Validate max price (if provided)
        if (notification.getMaxPrice() != null && notification.getMaxPrice() <= 0) {
            errors.add("Max price must be greater than 0");
        }

        // Validate validity period (if provided)
        if (notification.getValidityPeriod() != null && notification.getValidityPeriod() <= 0) {
            errors.add("Validity period must be greater than 0 seconds");
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    /**
     * Checks if a string contains only GSM-7 characters
     */
    private boolean isGSM7(String text) {
        // GSM-7 character set (basic + extension)
        String gsm7 = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?" +
                "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà" +
                "^{}\\[~]|€"; // Extension characters

        for (char c : text.toCharArray()) {
            if (gsm7.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }
}
