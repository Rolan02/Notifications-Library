package com.company.notifications.config.registry;

import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.provider.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing notification providers by channel.
 *
 * Design Pattern: Registry Pattern
 * - Centralized management of providers
 * - Easy lookup by channel
 * - Supports multiple providers per channel (future: load balancing, failover)
 */
public class ChannelProviderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProviderRegistry.class);

    private final Map<NotificationChannel, NotificationProvider<?>> providers;

    public ChannelProviderRegistry() {
        this.providers = new HashMap<>();
        logger.debug("ChannelProviderRegistry initialized");
    }

    /**
     * Registers a provider for a specific channel
     */
    public <T extends NotificationProvider<?>> void register(NotificationChannel channel, T provider) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        providers.put(channel, provider);
        logger.info("Registered provider {} for channel {}",
                provider.getProviderName(), channel);
    }

    /**
     * Gets the provider for a specific channel
     */
    public Optional<NotificationProvider<?>> getProvider(NotificationChannel channel) {
        return Optional.ofNullable(providers.get(channel));
    }

    /**
     * Checks if a provider is registered for the given channel
     */
    public boolean hasProvider(NotificationChannel channel) {
        return providers.containsKey(channel);
    }

    /**
     * Removes the provider for a specific channel
     */
    public void unregister(NotificationChannel channel) {
        NotificationProvider<?> removed = providers.remove(channel);
        if (removed != null) {
            logger.info("Unregistered provider {} for channel {}",
                    removed.getProviderName(), channel);
        }
    }

    /**
     * Clears all registered providers
     */
    public void clear() {
        logger.info("Clearing all registered providers");
        providers.clear();
    }

    /**
     * Gets the number of registered providers
     */
    public int size() {
        return providers.size();
    }

    /**
     * Checks if the registry is empty
     */
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Gets all registered channels
     */
    public java.util.Set<NotificationChannel> getRegisteredChannels() {
        return providers.keySet();
    }
}
