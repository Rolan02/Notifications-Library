package com.company.notifications.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publisher for notification events.
 *
 * Design Pattern: Observer Pattern (Pub/Sub)
 * - Maintains list of subscribers
 * - Publishes events to all subscribers
 * - Supports both sync and async publishing
 *
 * Thread-safe: Uses CopyOnWriteArrayList for concurrent access
 */
public class NotificationEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final List<NotificationEventListener> listeners;
    private final ExecutorService asyncExecutor;
    private final boolean asyncPublishing;

    /**
     * Creates a publisher with synchronous publishing
     */
    public NotificationEventPublisher() {
        this(false);
    }

    /**
     * Creates a publisher with configurable async publishing
     *
     * @param asyncPublishing If true, events are published asynchronously
     */
    public NotificationEventPublisher(boolean asyncPublishing) {
        this.listeners = new CopyOnWriteArrayList<>();
        this.asyncPublishing = asyncPublishing;
        this.asyncExecutor = asyncPublishing ?
                Executors.newFixedThreadPool(2, r -> {
                    Thread t = new Thread(r, "notification-event-publisher");
                    t.setDaemon(true);
                    return t;
                }) : null;

        logger.debug("NotificationEventPublisher initialized (async: {})", asyncPublishing);
    }

    /**
     * Subscribes a listener to receive events
     *
     * @param listener The listener to subscribe
     */
    public void subscribe(NotificationEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        listeners.add(listener);
        logger.debug("Subscribed listener (total: {})", listeners.size());
    }

    /**
     * Unsubscribes a listener
     *
     * @param listener The listener to unsubscribe
     */
    public void unsubscribe(NotificationEventListener listener) {
        if (listeners.remove(listener)) {
            logger.debug("Unsubscribed listener (remaining: {})", listeners.size());
        }
    }

    /**
     * Publishes an event to all subscribers
     *
     * @param event The event to publish
     */
    public void publish(NotificationEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }

        if (listeners.isEmpty()) {
            logger.trace("No listeners registered for event: {}", event.getEventType());
            return;
        }

        logger.debug("Publishing event: {} for notification {}",
                event.getEventType(), event.getNotificationId());

        if (asyncPublishing && asyncExecutor != null) {
            asyncExecutor.submit(() -> notifyListeners(event));
        } else {
            notifyListeners(event);
        }
    }

    /**
     * Notifies all listeners of an event
     */
    private void notifyListeners(NotificationEvent event) {
        for (NotificationEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                logger.error("Error in event listener for event {}: {}",
                        event.getEventType(), e.getMessage(), e);
                // Continue with other listeners even if one fails
            }
        }
    }

    /**
     * Gets the number of registered listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Clears all registered listeners
     */
    public void clearListeners() {
        logger.info("Clearing all event listeners");
        listeners.clear();
    }

    /**
     * Shuts down the publisher (if async)
     */
    public void shutdown() {
        if (asyncExecutor != null) {
            logger.info("Shutting down async event publisher");
            asyncExecutor.shutdown();
        }
    }
}
