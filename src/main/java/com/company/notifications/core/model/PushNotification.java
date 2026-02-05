package com.company.notifications.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Push notification specific implementation.
 * Extends Notification with push-specific fields like badge, sound, image, etc.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PushNotification extends Notification {

    private String title;
    private String deviceToken;

    @Builder.Default
    private PushPlatform platform = PushPlatform.ALL;

    private Integer badge;
    private String sound;
    private String imageUrl;
    private String clickAction;
    private String category;
    private Integer ttl;

    @Builder.Default
    private PushNotificationPriority priority = PushNotificationPriority.NORMAL;

    @Builder.Default
    private Map<String, String> data = new HashMap<>();

    @Builder.Default
    private boolean collapsible = false;

    private String collapseKey;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean hasRequiredFields() {
        // Push requires: device token (as recipient), title, and body
        return getRecipient() != null && !getRecipient().isBlank()
                && title != null && !title.isBlank()
                && getContent() != null
                && getContent().getBody() != null && !getContent().getBody().isBlank();
    }

    public void addData(String key, String value) {
        this.data.put(key, value);
    }
}
