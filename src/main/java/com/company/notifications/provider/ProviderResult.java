package com.company.notifications.provider;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Result returned by a NotificationProvider after attempting to send.
 *
 * This is the internal result from the provider layer.
 * It gets transformed into NotificationResult by the sender layer.
 *
 * Design Decision: Separate ProviderResult from NotificationResult
 * - ProviderResult: Raw response from provider (API-specific details)
 * - NotificationResult: Public API response (abstracted, user-friendly)
 */
@Data
@Builder
public class ProviderResult {
    private boolean success;
    private String providerMessageId;
    private Integer statusCode;
    private String message;
    private String errorCode;
    private Throwable exception;

    @Builder.Default
    private Instant timestamp = Instant.now();
    @Builder.Default
    private boolean retryable = false;
    @Builder.Default
    private Map<String, Object> rawResponse = new HashMap<>();
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    public static ProviderResult success(String providerMessageId, String message) {
        return ProviderResult.builder()
                .success(true)
                .providerMessageId(providerMessageId)
                .statusCode(200)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ProviderResult failure(String errorCode, String message) {
        return ProviderResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .retryable(false)
                .build();
    }
    public static ProviderResult retryableFailure(String errorCode, String message, Throwable exception) {
        return ProviderResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .exception(exception)
                .timestamp(Instant.now())
                .retryable(true)
                .build();
    }
    public void addRawResponse(String key, Object value) {
        this.rawResponse.put(key, value);
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
}
