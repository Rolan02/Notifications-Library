package com.company.notifications.core.validation;

import com.company.notifications.core.model.PushNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for push notifications.
 * Validates device tokens, titles, and push-specific constraints.
 */
public class PushNotificationValidator extends AbstractValidator<PushNotification> {

    // Push notification limits
    private static final int MAX_TITLE_LENGTH = 65;      // APNS limit
    private static final int MAX_BODY_LENGTH = 4096;     // FCM limit
    private static final int MAX_DATA_PAYLOAD_SIZE = 4096; // FCM limit (bytes)
    private static final int MAX_TTL_SECONDS = 2419200;  // 28 days
    private static final int MIN_TTL_SECONDS = 0;
    // Device token patterns
    private static final int FCM_TOKEN_LENGTH = 152;     // FCM tokens are typically ~152 chars
    private static final int APNS_TOKEN_LENGTH = 64;     // APNS tokens are 64 hex chars

    @Override
    public ValidationResult validate(PushNotification notification) {
        List<String> errors = new ArrayList<>();

        // Basic field validation
        ValidationResult basicValidation = validateBasicFields(notification);
        if (!basicValidation.isValid()) {
            errors.addAll(basicValidation.getErrors());
        }

        // Title validation (required for push)
        if (notification.getTitle() == null || notification.getTitle().isBlank()) {
            errors.add("Push notification title cannot be null or empty");
        } else if (notification.getTitle().length() > MAX_TITLE_LENGTH) {
            errors.add(String.format("Push notification title exceeds maximum length of %d characters (current: %d)",
                    MAX_TITLE_LENGTH, notification.getTitle().length()));
        }

        // Body length validation
        if (notification.getContent() != null && notification.getContent().getBody() != null) {
            int bodyLength = notification.getContent().getBody().length();
            if (bodyLength > MAX_BODY_LENGTH) {
                errors.add(String.format("Push notification body exceeds maximum length of %d characters (current: %d)",
                        MAX_BODY_LENGTH, bodyLength));
            }
        }

        // Device token validation
        if (notification.getRecipient() != null && !notification.getRecipient().isBlank()) {
            String deviceToken = notification.getRecipient().trim();

            // Basic validation - just check it's not empty and has reasonable length
            if (deviceToken.length() < 10) {
                errors.add("Device token is too short to be valid");
            } else if (deviceToken.length() > 500) {
                errors.add("Device token is too long to be valid");
            }

            // Platform-specific validation (if we know the platform)
            if (notification.getPlatform() != null) {
                switch (notification.getPlatform()) {
                    case IOS:
                        // APNS tokens are 64 hexadecimal characters
                        if (deviceToken.length() != APNS_TOKEN_LENGTH || !deviceToken.matches("[0-9a-fA-F]+")) {
                            errors.add("Invalid APNS device token format (expected 64 hex characters)");
                        }
                        break;
                    case ANDROID:
                        // FCM tokens are ~152 characters
                        if (deviceToken.length() < 100 || deviceToken.length() > 200) {
                            errors.add("Invalid FCM device token format (expected ~152 characters)");
                        }
                        break;
                    // WEB and ALL platforms are more flexible
                }
            }
        }

        // Badge validation (iOS specific)
        if (notification.getBadge() != null && notification.getBadge() < 0) {
            errors.add("Badge count cannot be negative");
        }

        // TTL validation
        if (notification.getTtl() != null) {
            if (notification.getTtl() < MIN_TTL_SECONDS) {
                errors.add(String.format("TTL cannot be less than %d seconds", MIN_TTL_SECONDS));
            } else if (notification.getTtl() > MAX_TTL_SECONDS) {
                errors.add(String.format("TTL cannot exceed %d seconds (28 days)", MAX_TTL_SECONDS));
            }
        }

        // Data payload size validation (approximate)
        if (notification.getData() != null && !notification.getData().isEmpty()) {
            int estimatedSize = estimateDataPayloadSize(notification.getData());
            if (estimatedSize > MAX_DATA_PAYLOAD_SIZE) {
                errors.add(String.format("Data payload size exceeds maximum of %d bytes (estimated: %d bytes)",
                        MAX_DATA_PAYLOAD_SIZE, estimatedSize));
            }
        }

        // Collapsible validation
        if (notification.isCollapsible() &&
                (notification.getCollapseKey() == null || notification.getCollapseKey().isBlank())) {
            errors.add("Collapse key is required when collapsible is true");
        }

        // Image URL validation (basic)
        if (notification.getImageUrl() != null && !notification.getImageUrl().isBlank()) {
            String imageUrl = notification.getImageUrl();
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                errors.add("Image URL must start with http:// or https://");
            }
        }

        // Click action validation (basic)
        if (notification.getClickAction() != null && !notification.getClickAction().isBlank()) {
            String clickAction = notification.getClickAction();
            // Basic validation - should be a URL or custom scheme
            if (!clickAction.contains("://") && !clickAction.startsWith("/")) {
                errors.add("Click action should be a valid URL or path");
            }
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private int estimateDataPayloadSize(java.util.Map<String, String> data) {
        int size = 0;
        for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
            // Rough estimate: key + value + JSON overhead
            size += entry.getKey().getBytes().length;
            size += entry.getValue().getBytes().length;
            size += 10; // JSON overhead (quotes, colons, commas)
        }
        return size;
    }
}
