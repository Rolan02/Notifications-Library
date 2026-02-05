package com.company.notifications.provider.sms;

import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.provider.NotificationProvider;

/**
 * Specialized interface for SMS providers.
 *
 * Implementations:
 * - MockTwilioProvider
 * - MockVonageProvider (formerly Nexmo)
 * - etc.
 *
 * All SMS providers must implement this interface, making them interchangeable.
 */
public interface SmsProvider extends NotificationProvider<SmsNotification> {

    default boolean supportsInternationalSms() {
        return true;
    }

    default boolean supportsDeliveryStatus() {
        return true;
    }

    default int getMaxMessageLength() {
        return 1600; // Allow up to 10 concatenated messages
    }

    default boolean supportsMms() {
        return false;
    }
}
