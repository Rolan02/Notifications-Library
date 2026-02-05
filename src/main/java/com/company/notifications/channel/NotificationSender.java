package com.company.notifications.channel;

import com.company.notifications.core.model.Notification;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main interface for sending notifications.
 * <p>
 * This is the public API that users of the library will interact with.
 * <p>
 * Design Pattern: Strategy Pattern + Facade
 * - Each channel (Email, SMS, Push) has its own sender implementation
 * - Abstracts away complexity of validation, provider selection, error handling
 * - Provides both sync and async sending capabilities
 * <p>
 * Responsibilities:
 * 1. Validate notification
 * 2. Select appropriate provider
 * 3. Send via provider
 * 4. Transform ProviderResult into NotificationResult
 * 5. Handle errors and retries
 * 6. Log and emit events
 */
public interface NotificationSender<T extends Notification> {

    /**
     * Sends a notification synchronously.
     *
     * @param notification The notification to send
     * @return NotificationResult indicating success or failure
     */
    NotificationResult send(T notification);

    /**
     * Sends a notification asynchronously.
     *
     * @param notification The notification to send
     * @return CompletableFuture that will complete with the result
     */
    default CompletableFuture<NotificationResult> sendAsync(T notification) {
        return CompletableFuture.supplyAsync(() -> send(notification));
    }

    /**
     * Sends multiple notifications in batch.
     *
     * @param notifications List of notifications to send
     * @return List of results in the same order as input
     */
    default List<NotificationResult> sendBatch(List<T> notifications) {
        return notifications.stream()
                .map(this::send)
                .toList();
    }

    /**
     * Sends multiple notifications asynchronously.
     *
     * @param notifications List of notifications to send
     * @return CompletableFuture that will complete with all results
     */
    default CompletableFuture<List<NotificationResult>> sendBatchAsync(List<T> notifications) {
        List<CompletableFuture<NotificationResult>> futures = notifications.stream()
                .map(this::sendAsync)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    NotificationChannel getChannel();

    boolean isReady();
}
