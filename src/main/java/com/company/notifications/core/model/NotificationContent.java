package com.company.notifications.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the content of a notification.
 * This is the actual message that will be delivered to the recipient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NotificationContent {

    private String body;
    private String title;

    /**
     * Additional metadata specific to the channel
     * For example:
     * - Email: htmlBody, plainTextBody
     * - Push: badge, sound, imageUrl
     * - SMS: shortCode
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Adds metadata to the content
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Gets metadata value by key
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    /**
     * Checks if metadata exists
     */
    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }
}
