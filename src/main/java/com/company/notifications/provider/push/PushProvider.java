package com.company.notifications.provider.push;

import com.company.notifications.core.model.PushNotification;
import com.company.notifications.provider.NotificationProvider;

/**
 * Specialized interface for push notification providers.
 *
 * Implementations:
 * - MockFirebaseProvider (FCM - Firebase Cloud Messaging)
 * - MockApnsProvider (Apple Push Notification Service)
 * - etc.
 *
 * All push providers must implement this interface, making them interchangeable.
 */
public interface PushProvider extends NotificationProvider<PushNotification>  {

    default boolean supportsIos() {
        return true;
    }

    default boolean supportsAndroid() {
        return true;
    }

    default boolean supportsWebPush() {
        return false;
    }

    default boolean supportsRichMedia() {
        return true;
    }

    default boolean supportsDataPayload() {
        return true;
    }

    default int getMaxPayloadSize() {
        return 4096; // 4 KB (FCM limit)
    }

    default int getMaxTtl() {
        return 2419200; // 28 days
    }
}
