package com.company.notifications.channel.push;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.PushNotification;
import com.company.notifications.core.validation.PushNotificationValidator;
import com.company.notifications.core.validation.ValidationResult;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.push.PushProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Push notification sender implementation.
 *
 * Responsibilities:
 * 1. Validate push notification (device token, title, body, payload size)
 * 2. Delegate to push provider (Firebase, APNS, etc.)
 * 3. Transform ProviderResult into NotificationResult
 * 4. Handle errors appropriately
 * 5. Log important events
 *
 * Design Pattern: Template Method (implicit)
 * - Same flow as Email and SMS: validate -> send -> transform result
 * - Demonstrates consistency across all channels
 */
public class PushNotificationSender implements NotificationSender<PushNotification> {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationSender.class);

    private final PushProvider provider;
    private final PushNotificationValidator validator;

    /**
     * Creates a push sender with the given provider
     */
    public PushNotificationSender(PushProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("PushProvider cannot be null");
        }

        if (!provider.isConfigured()) {
            throw new IllegalStateException("PushProvider is not properly configured");
        }

        this.provider = provider;
        this.validator = new PushNotificationValidator();

        logger.info("PushNotificationSender initialized with provider: {}", provider.getProviderName());
    }

    @Override
    public NotificationResult send(PushNotification notification) {
        String notificationId = notification.getMetadata().getNotificationId();

        logger.info("Sending push notification {} to device (platform: {})",
                notificationId, notification.getPlatform());

        try {
            // Step 1: Validate notification
            ValidationResult validationResult = validator.validate(notification);
            if (!validationResult.isValid()) {
                logger.warn("Push validation failed for {}: {}",
                        notificationId, validationResult.getErrors());

                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.PUSH,
                        notification.getRecipient(),
                        "VALIDATION_ERROR",
                        "Validation failed: " + validationResult.getFirstError()
                );
            }

            logger.debug("Push notification {} validated successfully", notificationId);

            // Step 2: Log payload size warning
            if (notification.getData() != null && !notification.getData().isEmpty()) {
                int estimatedSize = estimatePayloadSize(notification);
                if (estimatedSize > 3000) { // Warn if approaching 4KB limit
                    logger.warn("Push notification {} has large payload: ~{} bytes (max: 4096)",
                            notificationId, estimatedSize);
                }
            }

            // Step 3: Send via provider
            ProviderResult providerResult = provider.send(notification);

            // Step 4: Transform to NotificationResult
            NotificationResult result = transformProviderResult(
                    providerResult,
                    notification,
                    notificationId
            );

            // Step 5: Log result
            if (result.isSuccess()) {
                logger.info("Push notification {} sent successfully via {} (ID: {})",
                        notificationId,
                        provider.getProviderName(),
                        ((NotificationResult.Success) result).providerMessageId());
            } else {
                NotificationResult.Failure failure = (NotificationResult.Failure) result;
                logger.warn("Push notification {} failed: {} - {}",
                        notificationId,
                        failure.errorCode(),
                        failure.errorMessage());

                // Log special actions for certain errors
                if ("UNREGISTERED".equals(failure.errorCode())) {
                    logger.info("Device token should be removed from database: {}",
                            notification.getRecipient());
                }
            }

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error sending push notification {}: {}",
                    notificationId, e.getMessage(), e);

            return NotificationResult.failure(
                    notificationId,
                    NotificationChannel.PUSH,
                    notification.getRecipient(),
                    "UNEXPECTED_ERROR",
                    "Failed to send push notification: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean isReady() {
        return provider != null && provider.isConfigured();
    }

    /**
     * Transforms provider-specific result into public API result
     */
    private NotificationResult transformProviderResult(
            ProviderResult providerResult,
            PushNotification notification,
            String notificationId
    ) {
        if (providerResult.isSuccess()) {
            return NotificationResult.success(
                    notificationId,
                    providerResult.getProviderMessageId(),
                    NotificationChannel.PUSH,
                    notification.getRecipient(),
                    providerResult.getMessage()
            );
        } else {
            if (providerResult.isRetryable()) {
                return NotificationResult.retryableFailure(
                        notificationId,
                        NotificationChannel.PUSH,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            } else {
                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.PUSH,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            }
        }
    }

    /**
     * Estimates the payload size in bytes (approximate)
     */
    private int estimatePayloadSize(PushNotification notification) {
        int size = 0;

        // Notification part
        if (notification.getTitle() != null) {
            size += notification.getTitle().getBytes().length;
        }
        if (notification.getContent() != null && notification.getContent().getBody() != null) {
            size += notification.getContent().getBody().getBytes().length;
        }
        if (notification.getImageUrl() != null) {
            size += notification.getImageUrl().getBytes().length;
        }
        if (notification.getSound() != null) {
            size += notification.getSound().getBytes().length;
        }

        // Data payload
        if (notification.getData() != null) {
            for (var entry : notification.getData().entrySet()) {
                size += entry.getKey().getBytes().length;
                size += entry.getValue().getBytes().length;
                size += 10; // JSON overhead
            }
        }

        // Add overhead for JSON structure
        size += 200;

        return size;
    }

    /**
     * Gets the underlying push provider
     */
    public PushProvider getProvider() {
        return provider;
    }
}
