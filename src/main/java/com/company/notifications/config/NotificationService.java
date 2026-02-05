package com.company.notifications.config;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.email.EmailNotificationSender;
import com.company.notifications.channel.push.PushNotificationSender;
import com.company.notifications.channel.sms.SmsNotificationSender;
import com.company.notifications.config.registry.ChannelProviderRegistry;
import com.company.notifications.core.exception.ConfigurationException;
import com.company.notifications.core.model.*;
import com.company.notifications.provider.email.EmailProvider;
import com.company.notifications.provider.push.PushProvider;
import com.company.notifications.provider.sms.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main facade for the notification library.
 * Provides a unified API for sending notifications across all channels.
 *
 * Design Pattern: Facade Pattern
 * - Single entry point for all notification operations
 * - Hides complexity of channel-specific senders
 * - Automatic channel detection based on notification type
 *
 * Usage:
 * <pre>
 * NotificationService service = NotificationService.builder()
 *     .withEmailProvider(emailConfig)
 *     .withSmsProvider(smsConfig)
 *     .withPushProvider(pushConfig)
 *     .build();
 *
 * NotificationResult result = service.send(notification);
 * </pre>
 */
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationChannel, NotificationSender<?>> senders;
    private final ChannelProviderRegistry registry;

    /**
     * Private constructor - use builder to create instances
     */
    private NotificationService(
            Map<NotificationChannel, NotificationSender<?>> senders,
            ChannelProviderRegistry registry
    ) {
        this.senders = senders;
        this.registry = registry;

        logger.info("NotificationService initialized with {} channels", senders.size());
        senders.keySet().forEach(channel ->
                logger.debug("  - {} channel enabled", channel));
    }

    /**
     * Sends a notification (automatically detects channel)
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> NotificationResult send(T notification) {
        NotificationChannel channel = notification.getChannel();

        NotificationSender<T> sender = (NotificationSender<T>) senders.get(channel);
        if (sender == null) {
            String message = String.format("No sender configured for channel: %s", channel);
            logger.error(message);
            throw new ConfigurationException(message);
        }

        return sender.send(notification);
    }

    /**
     * Sends a notification asynchronously
     */
    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        return CompletableFuture.supplyAsync(() -> send(notification));
    }

    /**
     * Sends an email notification
     */
    public NotificationResult sendEmail(EmailNotification notification) {
        return send(notification);
    }

    /**
     * Sends an SMS notification
     */
    public NotificationResult sendSms(SmsNotification notification) {
        return send(notification);
    }

    /**
     * Sends a push notification
     */
    public NotificationResult sendPush(PushNotification notification) {
        return send(notification);
    }

    /**
     * Checks if a channel is available
     */
    public boolean isChannelAvailable(NotificationChannel channel) {
        return senders.containsKey(channel) && senders.get(channel).isReady();
    }

    /**
     * Gets the registry of providers
     */
    public ChannelProviderRegistry getRegistry() {
        return registry;
    }

    /**
     * Creates a new builder
     */
    public static NotificationServiceBuilder builder() {
        return new NotificationServiceBuilder();
    }

    /**
     * Builder for NotificationService
     */
    public static class NotificationServiceBuilder {
        private EmailProvider emailProvider;
        private SmsProvider smsProvider;
        private PushProvider pushProvider;

        private NotificationServiceBuilder() {
        }

        /**
         * Configures email provider
         */
        public NotificationServiceBuilder withEmailProvider(EmailProviderConfig config) {
            // In a real implementation, this would use a factory to create the provider
            // based on config type (SendGrid, Mailgun, etc.)
            throw new UnsupportedOperationException(
                    "Use withEmailProvider(EmailProvider) instead. " +
                            "Create provider manually with your preferred implementation."
            );
        }

        /**
         * Configures email provider
         */
        public NotificationServiceBuilder withEmailProvider(EmailProvider provider) {
            this.emailProvider = provider;
            return this;
        }

        /**
         * Configures SMS provider
         */
        public NotificationServiceBuilder withSmsProvider(SmsProviderConfig config) {
            throw new UnsupportedOperationException(
                    "Use withSmsProvider(SmsProvider) instead. " +
                            "Create provider manually with your preferred implementation."
            );
        }

        /**
         * Configures SMS provider
         */
        public NotificationServiceBuilder withSmsProvider(SmsProvider provider) {
            this.smsProvider = provider;
            return this;
        }

        /**
         * Configures push provider
         */
        public NotificationServiceBuilder withPushProvider(PushProviderConfig config) {
            throw new UnsupportedOperationException(
                    "Use withPushProvider(PushProvider) instead. " +
                            "Create provider manually with your preferred implementation."
            );
        }

        /**
         * Configures push provider
         */
        public NotificationServiceBuilder withPushProvider(PushProvider provider) {
            this.pushProvider = provider;
            return this;
        }

        /**
         * Builds the NotificationService
         */
        public NotificationService build() {
            Map<NotificationChannel, NotificationSender<?>> senders = new HashMap<>();
            ChannelProviderRegistry registry = new ChannelProviderRegistry();

            // Configure email if provided
            if (emailProvider != null) {
                if (!emailProvider.isConfigured()) {
                    throw new ConfigurationException("Email provider is not properly configured");
                }
                EmailNotificationSender emailSender = new EmailNotificationSender(emailProvider);
                senders.put(NotificationChannel.EMAIL, emailSender);
                registry.register(NotificationChannel.EMAIL, emailProvider);
            }

            // Configure SMS if provided
            if (smsProvider != null) {
                if (!smsProvider.isConfigured()) {
                    throw new ConfigurationException("SMS provider is not properly configured");
                }
                SmsNotificationSender smsSender = new SmsNotificationSender(smsProvider);
                senders.put(NotificationChannel.SMS, smsSender);
                registry.register(NotificationChannel.SMS, smsProvider);
            }

            // Configure push if provided
            if (pushProvider != null) {
                if (!pushProvider.isConfigured()) {
                    throw new ConfigurationException("Push provider is not properly configured");
                }
                PushNotificationSender pushSender = new PushNotificationSender(pushProvider);
                senders.put(NotificationChannel.PUSH, pushSender);
                registry.register(NotificationChannel.PUSH, pushProvider);
            }

            // Validate at least one channel is configured
            if (senders.isEmpty()) {
                throw new ConfigurationException(
                        "At least one notification channel must be configured"
                );
            }

            return new NotificationService(senders, registry);
        }
    }
}
