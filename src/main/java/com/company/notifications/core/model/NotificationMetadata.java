package com.company.notifications.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Metadata associated with a notification for tracking, auditing, and debugging purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NotificationMetadata {

    @Builder.Default
    private String notificationId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Optional correlation ID for tracking related notifications
     */
    private String correlationId;

    /**
     * Optional user ID who triggered this notification
     */
    private String userId;

    /**
     * Priority level (can be used for queuing)
     */
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * Additional custom tags for categorization and filtering
     */
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    /**
     * Adds a tag
     */
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    /**
     * Gets tag value
     */
    public String getTag(String key) {
        return this.tags.get(key);
    }
}
