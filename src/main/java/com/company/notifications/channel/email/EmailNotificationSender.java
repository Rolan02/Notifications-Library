package com.company.notifications.channel.email;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.validation.EmailNotificationValidator;
import com.company.notifications.core.validation.ValidationResult;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.email.EmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Email notification sender implementation.
 *
 * Responsibilities:
 * 1. Validate email notification
 * 2. Delegate to email provider
 * 3. Transform ProviderResult into NotificationResult
 * 4. Handle errors appropriately
 * 5. Log important events
 *
 * Design Pattern: Template Method (implicit)
 * - Common flow: validate -> send -> transform result
 * - Can be extracted to AbstractNotificationSender later
 */
public class EmailNotificationSender implements NotificationSender<EmailNotification> {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final EmailProvider provider;
    private final EmailNotificationValidator validator;

    /**
     * Creates an email sender with the given provider
     */
    public EmailNotificationSender(EmailProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("EmailProvider cannot be null");
        }

        if (!provider.isConfigured()) {
            throw new IllegalStateException("EmailProvider is not properly configured");
        }

        this.provider = provider;
        this.validator = new EmailNotificationValidator();

        logger.info("EmailNotificationSender initialized with provider: {}", provider.getProviderName());
    }

    @Override
    public NotificationResult send(EmailNotification notification) {
        String notificationId = notification.getMetadata().getNotificationId();

        logger.info("Sending email notification {} to {}", notificationId, notification.getRecipient());

        try {
            // Step 1: Validate notification
            ValidationResult validationResult = validator.validate(notification);
            if (!validationResult.isValid()) {
                logger.warn("Email validation failed for {}: {}",
                        notificationId, validationResult.getErrors());

                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.EMAIL,
                        notification.getRecipient(),
                        "VALIDATION_ERROR",
                        "Validation failed: " + validationResult.getFirstError()
                );
            }

            logger.debug("Email notification {} validated successfully", notificationId);

            // Step 2: Send via provider
            ProviderResult providerResult = provider.send(notification);

            // Step 3: Transform to NotificationResult
            NotificationResult result = transformProviderResult(
                    providerResult,
                    notification,
                    notificationId
            );

            // Step 4: Log result
            if (result.isSuccess()) {
                logger.info("Email notification {} sent successfully via {}",
                        notificationId, provider.getProviderName());
            } else {
                logger.warn("Email notification {} failed: {}",
                        notificationId,
                        ((NotificationResult.Failure) result).errorMessage());
            }

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error sending email notification {}: {}",
                    notificationId, e.getMessage(), e);

            return NotificationResult.failure(
                    notificationId,
                    NotificationChannel.EMAIL,
                    notification.getRecipient(),
                    "UNEXPECTED_ERROR",
                    "Failed to send email: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isReady() {
        return provider != null && provider.isConfigured();
    }

    private NotificationResult transformProviderResult(
            ProviderResult providerResult,
            EmailNotification notification,
            String notificationId
    ) {
        if (providerResult.isSuccess()) {
            return NotificationResult.success(
                    notificationId,
                    providerResult.getProviderMessageId(),
                    NotificationChannel.EMAIL,
                    notification.getRecipient(),
                    providerResult.getMessage()
            );
        } else {
            if (providerResult.isRetryable()) {
                return NotificationResult.retryableFailure(
                        notificationId,
                        NotificationChannel.EMAIL,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            } else {
                return NotificationResult.failure(
                        notificationId,
                        NotificationChannel.EMAIL,
                        notification.getRecipient(),
                        providerResult.getErrorCode(),
                        providerResult.getMessage(),
                        providerResult.getException()
                );
            }
        }
    }

    public EmailProvider getProvider() {
        return provider;
    }
}
