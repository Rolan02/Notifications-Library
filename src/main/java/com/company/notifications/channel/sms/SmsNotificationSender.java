package com.company.notifications.channel.sms;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.core.validation.SmsNotificationValidator;
import com.company.notifications.core.validation.ValidationResult;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.sms.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SMS notification sender implementation.
 *
 * Responsibilities:
 * 1. Validate SMS notification (phone number, message length, encoding)
 * 2. Delegate to SMS provider (Twilio, Vonage, etc.)
 * 3. Transform ProviderResult into NotificationResult
 * 4. Handle errors appropriately
 * 5. Log important events
 *
 * Design Pattern: Template Method (implicit)
 * - Same flow as EmailNotificationSender: validate -> send -> transform result
 * - Can be extracted to AbstractNotificationSender in the future
 */
public class SmsNotificationSender implements NotificationSender<SmsNotification> {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationSender.class);

    private final SmsProvider provider;
    private final SmsNotificationValidator validator;

    /**
     * Creates an SMS sender with the given provider
     */
    public SmsNotificationSender(SmsProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("SmsProvider cannot be null");
        }

        if (!provider.isConfigured()) {
            throw new IllegalStateException("SmsProvider is not properly configured");
        }

        this.provider = provider;
        this.validator = new SmsNotificationValidator();

        logger.info("SmsNotificationSender initialized with provider: {}", provider.getProviderName());
    }

    @Override
    public NotificationResult send(SmsNotification notification) {
        String notificationId = notification.getMetadata().getNotificationId();

        logger.info("Sending SMS notification {} to {}", notificationId, notification.getRecipient());

        try {
            // Step 1: Validate notification
            ValidationResult validationResult = validator.validate(notification);
            if (!validationResult.isValid()) {
                logger.warn("SMS validation failed for {}: {}",
                        notificationId, validationResult.getErrors());

                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.SMS,
                        notification.getRecipient(),
                        "VALIDATION_ERROR",
                        "Validation failed: " + validationResult.getFirstError()
                );
            }

            logger.debug("SMS notification {} validated successfully", notificationId);

            // Step 2: Check message length and warn about segments
            String body = notification.getContent().getBody();
            if (body.length() > 160) {
                int segments = (int) Math.ceil((double) body.length() / 153);
                logger.info("SMS notification {} will be sent as {} segments (length: {} chars)",
                        notificationId, segments, body.length());
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
                logger.info("SMS notification {} sent successfully via {} (ID: {})",
                        notificationId,
                        provider.getProviderName(),
                        ((NotificationResult.Success) result).providerMessageId());
            } else {
                NotificationResult.Failure failure = (NotificationResult.Failure) result;
                logger.warn("SMS notification {} failed: {} - {}",
                        notificationId,
                        failure.errorCode(),
                        failure.errorMessage());
            }

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error sending SMS notification {}: {}",
                    notificationId, e.getMessage(), e);

            return NotificationResult.failure(
                    notificationId,
                    NotificationChannel.SMS,
                    notification.getRecipient(),
                    "UNEXPECTED_ERROR",
                    "Failed to send SMS: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
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
            SmsNotification notification,
            String notificationId
    ) {
        if (providerResult.isSuccess()) {
            return NotificationResult.success(
                    notificationId,
                    providerResult.getProviderMessageId(),
                    NotificationChannel.SMS,
                    notification.getRecipient(),
                    providerResult.getMessage()
            );
        } else {
            if (providerResult.isRetryable()) {
                return NotificationResult.retryableFailure(
                        notificationId,
                        NotificationChannel.SMS,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            } else {
                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.SMS,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            }
        }
    }

    /**
     * Gets the underlying SMS provider
     */
    public SmsProvider getProvider() {
        return provider;
    }
}
