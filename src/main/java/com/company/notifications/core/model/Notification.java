package com.company.notifications.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all notifications.
 * This represents what a notification IS - the core domain concept.
 * Design Decision: Abstract base class approach
 * - Provides common fields (recipient, content, metadata, channel)
 * - Each channel extends this with specific fields
 * - Enforces consistency across all notification types
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Notification {

    private String recipient;
    private NotificationContent content;

    @Builder.Default
    private NotificationMetadata metadata = NotificationMetadata.builder().build();

    /**
     * Returns the channel this notification belongs to.
     * Must be implemented by each subclass.
     */
    public abstract NotificationChannel getChannel();

    /**
     * Validates that the notification has all required fields.
     * Can be overridden by subclasses for specific validation.
     */
    public boolean hasRequiredFields() {
        return recipient != null && !recipient.isBlank()
                && content != null
                && content.getBody() != null && !content.getBody().isBlank();
    }
}
