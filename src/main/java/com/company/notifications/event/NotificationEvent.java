package com.company.notifications.event;

import com.company.notifications.core.model.NotificationChannel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Event published during notification lifecycle.
 *
 * Design Pattern: Observer Pattern (Event/Message)
 * - Carries information about notification state changes
 * - Immutable after creation
 * - Can be consumed by multiple listeners
 */
@Data
@Builder
public class NotificationEvent {

    private final NotificationEventType eventType;
    private final String notificationId;
    private final NotificationChannel channel;
    private final String recipient;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final String message;
    private final String errorCode;
    private final String errorMessage;
    private final String providerMessageId;

    @Builder.Default
    private final Map<String, String> metadata = new HashMap<>();

    @Builder.Default
    private final boolean isRetry = false;

    private final Integer retryAttempt;
}
