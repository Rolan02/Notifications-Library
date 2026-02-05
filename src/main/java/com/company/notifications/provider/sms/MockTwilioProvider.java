package com.company.notifications.provider.sms;

import com.company.notifications.config.SmsProviderConfig;
import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.provider.ProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of Twilio SMS provider.
 * Simulates Twilio API behavior without making real HTTP calls.
 *
 * In a real implementation, this would:
 * - Make HTTP POST to https://api.twilio.com/2010-04-01/Accounts/{AccountSid}/Messages.json
 * - Handle authentication via Basic Auth (AccountSid + AuthToken)
 * - Transform SmsNotification into Twilio's form data format
 * - Parse response and handle Twilio-specific error codes
 *
 * This mock simulates:
 * - Successful sends (92% of the time)
 * - Invalid phone numbers (3%)
 * - Rate limits (3%)
 * - Network failures (2%)
 *
 * Twilio-specific details:
 * - Message SID format: SM + 32 hex characters
 * - Status codes: queued, sending, sent, delivered, failed
 * - Price calculation for international SMS
 */
public class MockTwilioProvider implements SmsProvider{

    private static final Logger logger = LoggerFactory.getLogger(MockTwilioProvider.class);
    private static final Random random = new Random();

    private final SmsProviderConfig config;

    public MockTwilioProvider(SmsProviderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SmsProviderConfig cannot be null");
        }
        this.config = config;

        logger.info("Initialized MockTwilioProvider with Account SID: {}", config.getAccountSidMasked());
    }

    @Override
    public ProviderResult send(SmsNotification notification) {
        logger.info("MockTwilioProvider: Sending SMS to {} from {}",
                notification.getRecipient(),
                config.getFromPhoneNumber() != null ? config.getFromPhoneNumber() : config.getShortCode());

        // Simulate network latency (slightly faster than email)
        simulateLatency();

        // Check if using test credentials
        if (config.isUseTestCredentials()) {
            logger.info("MockTwilioProvider: Using test credentials, SMS not actually sent");
            return createSuccessResult(notification, true);
        }

        // Simulate different outcomes
        int outcome = random.nextInt(100);

        if (outcome < 92) {
            // 92% success rate (slightly higher than email)
            return createSuccessResult(notification, false);
        } else if (outcome < 95) {
            // 3% invalid phone numbers
            return createInvalidPhoneFailure(notification);
        } else if (outcome < 98) {
            // 3% rate limit failures (retryable)
            return createRateLimitFailure(notification);
        } else {
            // 2% network/service failures (retryable)
            return createNetworkFailure(notification);
        }
    }

    @Override
    public String getProviderName() {
        return "Twilio";
    }

    @Override
    public String getProviderType() {
        return "sms";
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

        logger.info("MockTwilioProvider: Health check - simulating API ping");
        simulateLatency();

        // Simulate 97% uptime (SMS providers are very reliable)
        return random.nextInt(100) < 97;
    }

    /**
     * Creates a successful result simulating Twilio's response
     */
    private ProviderResult createSuccessResult(SmsNotification notification, boolean testMode) {
        // Twilio Message SID format: SM + 32 hex characters
        String messageSid = "SM" + UUID.randomUUID().toString().replace("-", "");

        // Calculate approximate price (simulation)
        double price = calculatePrice(notification.getRecipient());

        ProviderResult result = ProviderResult.builder()
                .success(true)
                .providerMessageId(messageSid)
                .statusCode(201) // Twilio returns 201 Created
                .message(testMode ? "SMS queued (test mode)" : "SMS queued for delivery")
                .timestamp(Instant.now())
                .build();

        // Add Twilio-specific metadata
        result.addMetadata("provider", "Twilio");
        result.addMetadata("test_mode", String.valueOf(testMode));
        result.addMetadata("status", "queued");
        result.addMetadata("price", String.format("%.4f", price));
        result.addMetadata("price_unit", "USD");
        result.addMetadata("direction", "outbound-api");

        if (config.isRequestDeliveryStatus() && config.getStatusCallbackUrl() != null) {
            result.addMetadata("status_callback", config.getStatusCallbackUrl());
        }

        // Add raw response simulation (Twilio returns JSON)
        result.addRawResponse("sid", messageSid);
        result.addRawResponse("status", "queued");
        result.addRawResponse("to", notification.getRecipient());
        result.addRawResponse("from", getFromNumber());
        result.addRawResponse("body", notification.getContent().getBody());
        result.addRawResponse("num_segments", calculateSegments(notification.getContent().getBody()));
        result.addRawResponse("price", String.format("%.4f", price));

        logger.info("MockTwilioProvider: SMS sent successfully with SID {}, price: ${}",
                messageSid.substring(0, 10) + "...", String.format("%.4f", price));

        return result;
    }

    /**
     * Simulates invalid phone number error
     */
    private ProviderResult createInvalidPhoneFailure(SmsNotification notification) {
        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("21211")
                .message("The 'To' number " + notification.getRecipient() + " is not a valid phone number")
                .statusCode(400) // Bad Request
                .timestamp(Instant.now())
                .retryable(false) // Invalid phone numbers are not retryable
                .build();

        result.addMetadata("provider", "Twilio");
        result.addMetadata("error_type", "validation");
        result.addMetadata("twilio_error_code", "21211");

        logger.warn("MockTwilioProvider: Invalid phone number - {}", notification.getRecipient());

        return result;
    }

    /**
     * Simulates rate limit exceeded error
     */
    private ProviderResult createRateLimitFailure(SmsNotification notification) {
        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("20429")
                .message("Too many requests - rate limit exceeded")
                .statusCode(429) // Too Many Requests
                .timestamp(Instant.now())
                .retryable(true) // Rate limits are retryable
                .build();

        result.addMetadata("provider", "Twilio");
        result.addMetadata("error_type", "rate_limit");
        result.addMetadata("twilio_error_code", "20429");
        result.addMetadata("retry_after", "60"); // seconds

        logger.warn("MockTwilioProvider: Rate limit exceeded for {}", notification.getRecipient());

        return result;
    }

    /**
     * Simulates network/service failure
     */
    private ProviderResult createNetworkFailure(SmsNotification notification) {
        String[] errors = {
                "Service temporarily unavailable",
                "Gateway timeout",
                "Connection refused"
        };

        String message = errors[random.nextInt(errors.length)];

        ProviderResult result = ProviderResult.builder()
                .success(false)
                .errorCode("NETWORK_ERROR")
                .message(message)
                .statusCode(503) // Service Unavailable
                .timestamp(Instant.now())
                .retryable(true)
                .build();

        result.addMetadata("provider", "Twilio");
        result.addMetadata("error_type", "transient");

        logger.error("MockTwilioProvider: Network failure - {} for {}", message, notification.getRecipient());

        return result;
    }

    /**
     * Simulates network latency (30-150ms - faster than email)
     */
    private void simulateLatency() {
        try {
            Thread.sleep(30 + random.nextInt(120));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculates price based on destination (simulation)
     */
    private double calculatePrice(String phoneNumber) {
        // Domestic SMS: ~$0.0075
        // International SMS: ~$0.02 - $0.15

        if (phoneNumber.startsWith("+1")) {
            // US/Canada
            return 0.0075;
        } else if (phoneNumber.startsWith("+44") || phoneNumber.startsWith("+49") ||
                phoneNumber.startsWith("+33") || phoneNumber.startsWith("+39")) {
            // Europe
            return 0.045 + (random.nextDouble() * 0.03);
        } else {
            // Other international
            return 0.08 + (random.nextDouble() * 0.07);
        }
    }

    /**
     * Calculates number of SMS segments
     */
    private int calculateSegments(String body) {
        if (body == null || body.isEmpty()) {
            return 1;
        }

        // Simple calculation (real implementation would check encoding)
        int length = body.length();

        if (length <= 160) {
            return 1;
        } else {
            // Concatenated SMS uses 153 characters per segment
            return (int) Math.ceil((double) length / 153);
        }
    }

    /**
     * Gets the from number (phone number or short code)
     */
    private String getFromNumber() {
        if (config.getFromPhoneNumber() != null && !config.getFromPhoneNumber().isBlank()) {
            return config.getFromPhoneNumber();
        }
        return config.getShortCode();
    }
}
