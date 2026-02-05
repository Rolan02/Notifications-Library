package com.company.notifications.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base configuration for notification providers.
 * Each provider type extends this with specific configuration fields.
 * <p>
 * Design Decision: Abstract base class with SuperBuilder
 * - Allows type-safe configuration per provider
 * - SuperBuilder enables builder pattern in subclasses
 * - Common fields (enabled, timeout) inherited by all
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ProviderConfig {

    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private int connectionTimeoutMs = 30000; // 30 seconds
    @Builder.Default
    private int readTimeoutMs = 30000; // 30 seconds
    @Builder.Default
    private boolean testMode = false;
    @Builder.Default
    private int maxRetries = 3;

    public abstract boolean isValid();

    public abstract String getProviderType();

}
