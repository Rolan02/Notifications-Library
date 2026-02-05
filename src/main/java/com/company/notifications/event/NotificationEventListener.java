package com.company.notifications.event;

/**
 * Listener interface for notification events.
 *
 * Design Pattern: Observer Pattern
 * - Implement this interface to receive notification events
 * - Multiple listeners can subscribe to the same publisher
 * - Listeners are called synchronously in the order they were registered
 *
 * Usage:
 * <pre>
 * publisher.subscribe(event -> {
 *     if (event.getEventType() == NotificationEventType.NOTIFICATION_SENT) {
 *         logger.info("Notification sent: {}", event.getNotificationId());
 *     }
 * });
 * </pre>
 */
@FunctionalInterface
public interface NotificationEventListener {

    /**
     * Called when a notification event occurs
     *
     * @param event The notification event
     */
    void onEvent(NotificationEvent event);
}
