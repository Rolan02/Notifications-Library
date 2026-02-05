package com.company.notifications.provider.email;

import com.company.notifications.config.EmailProviderConfig;
import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.provider.ProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of SendGrid email provider.
 * Simulates SendGrid API behavior without making real HTTP calls.
 *
 * In a real implementation, this would:
 * - Make HTTP POST to https://api.sendgrid.com/v3/mail/send
 * - Handle authentication via API key
 * - Transform EmailNotification into SendGrid's JSON format
 * - Parse response and handle errors
 *
 * This mock simulates:
 * - Successful sends (90% of the time)
 * - Transient failures that can be retried (5%)
 * - Permanent failures (5%)
 */
public class MockSendGridProvider implements EmailProvider  {

    private static final Logger logger = LoggerFactory.getLogger(MockSendGridProvider.class);
    private static final Random random = new Random();

    private final EmailProviderConfig config;

    public MockSendGridProvider(EmailProviderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("EmailProviderConfig cannot be null");
        }
        this.config = config;

        logger.info("Initialized MockSendGridProvider with API key: {}", config.getApiKeyMasked());
    }

    @Override
    public ProviderResult send(EmailNotification notification) {
        logger.info("MockSendGridProvider: Sending email to {} with subject '{}'",
                notification.getRecipient(), notification.getSubject());

        // Simulate network latency
        simulateLatency();

        // Check if sandbox mode
        if (config.isSandboxMode()) {
            logger.info("MockSendGridProvider: Sandbox mode enabled, email not actually sent");
            return createSuccessResult(notification, true);
        }

        // Simulate different outcomes
        int outcome = random.nextInt(100);

        if (outcome < 90) {
            // 90% success rate
            return createSuccessResult(notification, false);
        } else if (outcome < 95) {
            // 5% transient failures (retryable)
            return createTransientFailure(notification);
        } else {
            // 5% permanent failures
            return createPermanentFailure(notification);
        }
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }

    @Override
    public String getProviderType() {
        return "email";
    }

    @Override
    public boolean isConfigured() {
        return config != null && config.isValid();
    }

    @Override
    public boolean healthCheck() {
        if (!isConfigured()) {
            return false;
        }

        logger.info("MockSendGridProvider: Health check - simulating API ping");
        simulateLatency();

        // Simulate 95% uptime
        return random.nextInt(100) < 95;
    }

    private ProviderResult createSuccessResult(EmailNotification notification, boolean sandbox) {
        String messageId = "sg_" + UUID.randomUUID().toString();

        ProviderResult result = ProviderResult.builder()
                .success(true)
                .providerMessageId(messageId)
                .statusCode(202) // SendGrid returns 202 Accepted
                .message(sandbox ? "Email queued (sandbox mode)" : "Email queued for delivery")
                .timestamp(Instant.now())
                .build();

        // Add SendGrid-specific metadata
        result.addMetadata("provider", "SendGrid");
        result.addMetadata("sandbox", String.valueOf(sandbox));
        result.addMetadata("tracking_opens", String.valueOf(config.isTrackOpens()));
        result.addMetadata("tracking_clicks", String.valueOf(config.isTrackClicks()));

        // Add raw response simulation
        result.addRawResponse("message_id", messageId);
        result.addRawResponse("status", "queued");
        result.addRawResponse("recipient", notification.getRecipient());
        result.addRawResponse("subject", notification.getSubject());

        logger.info("MockSendGridProvider: Email sent successfully with ID {}", messageId);

        return result;
    }

    private ProviderResult createTransientFailure(EmailNotification notification) {
        String[] transientErrors = {
                "RATE_LIMIT_EXCEEDED",
                "SERVICE_TEMPORARILY_UNAVAILABLE",
                "GATEWAY_TIMEOUT"
        };

        String errorCode = transientErrors[random.nextInt(transientErrors.length)];

        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message("SendGrid API temporarily unavailable, please retry")
                .statusCode(429) // Too Many Requests
                .timestamp(Instant.now())
                .retryable(true)
                .build();

        result.addMetadata("provider", "SendGrid");
        result.addMetadata("error_type", "transient");

        logger.warn("MockSendGridProvider: Transient failure - {} for recipient {}",
                errorCode, notification.getRecipient());

        return result;
    }

    private ProviderResult createPermanentFailure(EmailNotification notification) {
        String[] permanentErrors = {
                "INVALID_EMAIL",
                "RECIPIENT_BLOCKED",
                "DOMAIN_NOT_FOUND",
                "SPAM_DETECTED"
        };

        String errorCode = permanentErrors[random.nextInt(permanentErrors.length)];

        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message("Failed to send email: " + errorCode)
                .statusCode(400) // Bad Request
                .timestamp(Instant.now())
                .retryable(false)
                .build();

        result.addMetadata("provider", "SendGrid");
        result.addMetadata("error_type", "permanent");

        logger.error("MockSendGridProvider: Permanent failure - {} for recipient {}",
                errorCode, notification.getRecipient());

        return result;
    }

    /**
     * Simulates network latency (50-200ms)
     */
    private void simulateLatency() {
        try {
            Thread.sleep(50 + random.nextInt(150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
