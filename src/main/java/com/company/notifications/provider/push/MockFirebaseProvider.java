package com.company.notifications.provider.push;

import com.company.notifications.config.PushProviderConfig;
import com.company.notifications.core.model.PushNotification;
import com.company.notifications.provider.ProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of Firebase Cloud Messaging (FCM) provider.
 * Simulates FCM API behavior without making real HTTP calls.
 *
 * In a real implementation, this would:
 * - Make HTTP POST to https://fcm.googleapis.com/v1/projects/{project-id}/messages:send
 * - Handle authentication via OAuth 2.0 or Server Key
 * - Transform PushNotification into FCM's JSON format
 * - Parse response and handle FCM-specific error codes
 *
 * This mock simulates:
 * - Successful sends (88% of the time)
 * - Invalid device tokens (5%)
 * - Quota exceeded (3%)
 * - Network failures (2%)
 * - Unregistered devices (2%)
 *
 * FCM-specific details:
 * - Message ID format: projects/{project}/messages/{id}
 * - Supports Android, iOS, and Web
 * - TTL up to 28 days
 * - Priority: high or normal
 */
public class MockFirebaseProvider implements PushProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockFirebaseProvider.class);
    private static final Random random = new Random();

    private final PushProviderConfig config;

    public MockFirebaseProvider(PushProviderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("PushProviderConfig cannot be null");
        }
        this.config = config;

        logger.info("Initialized MockFirebaseProvider with Server Key: {}", config.getServerKeyMasked());
        if (config.getProjectId() != null) {
            logger.info("  Project ID: {}", config.getProjectId());
        }
    }

    @Override
    public ProviderResult send(PushNotification notification) {
        logger.info("MockFirebaseProvider: Sending push to device token {} with title '{}'",
                maskToken(notification.getRecipient()), notification.getTitle());

        // Simulate network latency (20-100ms - very fast)
        simulateLatency();

        // Check if dry run mode
        if (config.isDryRun()) {
            logger.info("MockFirebaseProvider: Dry run mode enabled, notification validated but not sent");
            return createDryRunResult(notification);
        }

        // Check if sandbox mode
        if (config.isUseSandbox()) {
            logger.info("MockFirebaseProvider: Sandbox mode enabled, notification not actually sent");
            return createSuccessResult(notification, true);
        }

        // Simulate different outcomes
        int outcome = random.nextInt(100);

        if (outcome < 88) {
            // 88% success rate
            return createSuccessResult(notification, false);
        } else if (outcome < 93) {
            // 5% invalid device tokens
            return createInvalidTokenFailure(notification);
        } else if (outcome < 96) {
            // 3% quota exceeded (retryable)
            return createQuotaExceededFailure(notification);
        } else if (outcome < 98) {
            // 2% network failures (retryable)
            return createNetworkFailure(notification);
        } else {
            // 2% unregistered devices (permanent)
            return createUnregisteredFailure(notification);
        }
    }

    @Override
    public String getProviderName() {
        return "Firebase Cloud Messaging";
    }

    @Override
    public String getProviderType() {
        return "push";
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

        logger.info("MockFirebaseProvider: Health check - simulating FCM API ping");
        simulateLatency();

        // FCM has very high uptime (99.9%)
        return random.nextInt(1000) < 999;
    }

    @Override
    public boolean supportsWebPush() {
        return true; // FCM supports web push
    }

    /**
     * Creates a successful result simulating FCM's response
     */
    private ProviderResult createSuccessResult(PushNotification notification, boolean sandbox) {
        // FCM message ID format: projects/{project}/messages/{id}
        String projectId = config.getProjectId() != null ? config.getProjectId() : "mock-project";
        String messageId = String.format("projects/%s/messages/%s", projectId, generateMessageId());

        ProviderResult result = ProviderResult.builder()
                .success(true)
                .providerMessageId(messageId)
                .statusCode(200) // FCM returns 200 OK
                .message(sandbox ? "Push notification queued (sandbox mode)" : "Push notification sent successfully")
                .timestamp(Instant.now())
                .build();

        // Add FCM-specific metadata
        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("sandbox", String.valueOf(sandbox));
        result.addMetadata("platform", notification.getPlatform().toString());
        result.addMetadata("priority", notification.getPriority().toString());

        if (notification.getTtl() != null) {
            result.addMetadata("ttl", String.valueOf(notification.getTtl()));
        }

        if (config.isEnableAnalytics()) {
            result.addMetadata("analytics_enabled", "true");
            result.addMetadata("analytics_label", notification.getCategory() != null ?
                    notification.getCategory() : "default");
        }

        // Add raw response simulation (FCM returns JSON)
        result.addRawResponse("name", messageId);
        result.addRawResponse("token", notification.getRecipient());
        result.addRawResponse("notification", buildNotificationObject(notification));

        if (!notification.getData().isEmpty()) {
            result.addRawResponse("data", notification.getData());
        }

        logger.info("MockFirebaseProvider: Push sent successfully with ID {}",
                messageId.substring(messageId.lastIndexOf('/') + 1));

        return result;
    }

    /**
     * Creates a dry run result (validation only)
     */
    private ProviderResult createDryRunResult(PushNotification notification) {
        String messageId = "dry-run-" + UUID.randomUUID().toString();

        ProviderResult result = ProviderResult.builder()
                .success(true)
                .providerMessageId(messageId)
                .statusCode(200)
                .message("Notification validated successfully (dry run)")
                .timestamp(Instant.now())
                .build();

        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("dry_run", "true");
        result.addMetadata("validation_status", "passed");

        logger.info("MockFirebaseProvider: Dry run validation successful");

        return result;
    }

    /**
     * Simulates invalid device token error
     */
    private ProviderResult createInvalidTokenFailure(PushNotification notification) {
        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("INVALID_ARGUMENT")
                .message("The registration token is not a valid FCM registration token")
                .statusCode(400) // Bad Request
                .timestamp(Instant.now())
                .retryable(false) // Invalid tokens are not retryable
                .build();

        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("error_type", "validation");
        result.addMetadata("fcm_error_code", "INVALID_ARGUMENT");

        logger.warn("MockFirebaseProvider: Invalid device token - {}",
                maskToken(notification.getRecipient()));

        return result;
    }

    /**
     * Simulates quota exceeded error
     */
    private ProviderResult createQuotaExceededFailure(PushNotification notification) {
        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("QUOTA_EXCEEDED")
                .message("Messaging quota exceeded for this project")
                .statusCode(429) // Too Many Requests
                .timestamp(Instant.now())
                .retryable(true) // Quota limits are retryable
                .build();

        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("error_type", "quota");
        result.addMetadata("fcm_error_code", "QUOTA_EXCEEDED");
        result.addMetadata("retry_after", "60"); // seconds

        logger.warn("MockFirebaseProvider: Quota exceeded");

        return result;
    }

    /**
     * Simulates network/service failure
     */
    private ProviderResult createNetworkFailure(PushNotification notification) {
        String[] errors = {
                "Service temporarily unavailable",
                "Internal server error",
                "Gateway timeout"
        };

        String message = errors[random.nextInt(errors.length)];

        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("UNAVAILABLE")
                .message(message)
                .statusCode(503) // Service Unavailable
                .timestamp(Instant.now())
                .retryable(true)
                .build();

        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("error_type", "transient");
        result.addMetadata("fcm_error_code", "UNAVAILABLE");

        logger.error("MockFirebaseProvider: Network failure - {}", message);

        return result;
    }

    /**
     * Simulates unregistered device error
     */
    private ProviderResult createUnregisteredFailure(PushNotification notification) {
        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("UNREGISTERED")
                .message("App instance was unregistered from FCM. This usually means the app was uninstalled.")
                .statusCode(404) // Not Found
                .timestamp(Instant.now())
                .retryable(false) // Unregistered devices should be removed from database
                .build();

        result.addMetadata("provider", "Firebase Cloud Messaging");
        result.addMetadata("error_type", "permanent");
        result.addMetadata("fcm_error_code", "UNREGISTERED");
        result.addMetadata("action", "remove_token");

        logger.warn("MockFirebaseProvider: Device unregistered - {}",
                maskToken(notification.getRecipient()));

        return result;
    }

    /**
     * Simulates network latency (20-100ms - push is very fast)
     */
    private void simulateLatency() {
        try {
            Thread.sleep(20 + random.nextInt(80));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Generates a unique message ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Builds notification object for raw response
     */
    private Object buildNotificationObject(PushNotification notification) {
        return new java.util.HashMap<String, Object>() {{
            put("title", notification.getTitle());
            put("body", notification.getContent().getBody());
            if (notification.getImageUrl() != null) {
                put("image", notification.getImageUrl());
            }
            if (notification.getSound() != null) {
                put("sound", notification.getSound());
            }
            if (notification.getBadge() != null) {
                put("badge", notification.getBadge());
            }
        }};
    }

    /**
     * Masks device token for logging (security)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }
}
