package com.company.notifications.provider;

import com.company.notifications.core.model.Notification;

/**
 * Base interface for all notification providers.
 *
 * A provider is responsible for:
 * - Communicating with external APIs (SendGrid, Twilio, Firebase, etc.)
 * - Transforming our notification model into provider-specific format
 * - Handling provider-specific authentication and configuration
 * - Returning standardized ProviderResult
 *
 * Design Pattern: Strategy Pattern
 * - Different providers (SendGrid, Mailgun) implement the same interface
 * - Easy to switch providers without changing sender logic
 * - New providers can be added without modifying existing code (OCP)
 */
public interface NotificationProvider<T extends Notification> {

    ProviderResult send(T notification);

    String getProviderName();

    String getProviderType();

    boolean isConfigured();

    default boolean healthCheck() {
        return isConfigured();
    }
}
