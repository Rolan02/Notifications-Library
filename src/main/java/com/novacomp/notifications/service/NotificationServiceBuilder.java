package com.novacomp.notifications.service;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.config.RetryPolicy;
import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.event.NotificationEventListener;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.email.EmailProvider;
import com.novacomp.notifications.provider.push.PushProvider;
import com.novacomp.notifications.provider.slack.SlackProvider;
import com.novacomp.notifications.provider.sms.SmsProvider;
import com.novacomp.notifications.sender.EmailNotificationSender;
import com.novacomp.notifications.sender.NotificationSender;
import com.novacomp.notifications.sender.PushNotificationSender;
import com.novacomp.notifications.sender.RetryableNotificationSender;
import com.novacomp.notifications.sender.SlackNotificationSender;
import com.novacomp.notifications.sender.SmsNotificationSender;
import com.novacomp.notifications.validation.EmailValidator;
import com.novacomp.notifications.validation.NotificationValidator;
import com.novacomp.notifications.validation.PhoneValidator;
import com.novacomp.notifications.validation.PushValidator;
import com.novacomp.notifications.validation.SlackValidator;

import java.util.EnumMap;
import java.util.Map;

/**
 * Builder (patron Builder) para ensamblar un {@link NotificationService}
 * eligiendo, por canal, que proveedor concreto usar (SendGrid vs Mailgun,
 * etc.) y si se envuelve con reintentos. Este es el UNICO lugar de la
 * libreria donde se conocen las clases concretas de proveedor.
 */
public final class NotificationServiceBuilder {

    private final Map<NotificationChannel, NotificationSender<? extends Notification>> senders =
            new EnumMap<>(NotificationChannel.class);
    private final NotificationEventPublisher eventPublisher = new NotificationEventPublisher();

    public static NotificationServiceBuilder create() {
        return new NotificationServiceBuilder();
    }

    // ---- Email ----

    public NotificationServiceBuilder registerEmailSender(EmailProvider provider) {
        return registerEmailSender(provider, new EmailValidator());
    }

    public NotificationServiceBuilder registerEmailSender(EmailProvider provider,
                                                           NotificationValidator<EmailNotification> validator) {
        return registerSender(new EmailNotificationSender(provider, validator, eventPublisher));
    }

    // ---- SMS ----

    public NotificationServiceBuilder registerSmsSender(SmsProvider provider) {
        return registerSmsSender(provider, new PhoneValidator());
    }

    public NotificationServiceBuilder registerSmsSender(SmsProvider provider,
                                                         NotificationValidator<SmsNotification> validator) {
        return registerSender(new SmsNotificationSender(provider, validator, eventPublisher));
    }

    // ---- Push ----

    public NotificationServiceBuilder registerPushSender(PushProvider provider) {
        return registerPushSender(provider, new PushValidator());
    }

    public NotificationServiceBuilder registerPushSender(PushProvider provider,
                                                          NotificationValidator<PushNotification> validator) {
        return registerSender(new PushNotificationSender(provider, validator, eventPublisher));
    }

    // ---- Slack (opcional) ----

    public NotificationServiceBuilder registerSlackSender(SlackProvider provider) {
        return registerSlackSender(provider, new SlackValidator());
    }

    public NotificationServiceBuilder registerSlackSender(SlackProvider provider,
                                                           NotificationValidator<SlackNotification> validator) {
        return registerSender(new SlackNotificationSender(provider, validator, eventPublisher));
    }

    // ---- Generico: agregar un canal nuevo sin tocar esta clase (Open/Closed) ----

    public NotificationServiceBuilder registerSender(NotificationSender<? extends Notification> sender) {
        senders.put(sender.getChannel(), sender);
        return this;
    }

    /** Envuelve el sender ya registrado de un canal con reintentos + backoff. */
    public NotificationServiceBuilder withRetry(NotificationChannel channel, RetryPolicy policy) {
        NotificationSender<? extends Notification> existing = senders.get(channel);
        if (existing == null) {
            throw new IllegalStateException(
                    "Registra un sender para " + channel + " antes de aplicar withRetry(...)");
        }
        senders.put(channel, wrapWithRetry(existing, policy));
        return this;
    }

    public NotificationServiceBuilder addEventListener(NotificationEventListener listener) {
        eventPublisher.subscribe(listener);
        return this;
    }

    public NotificationService build() {
        return new NotificationService(new EnumMap<>(senders), eventPublisher);
    }

    private <T extends Notification> NotificationSender<T> wrapWithRetry(NotificationSender<T> sender,
                                                                          RetryPolicy policy) {
        return new RetryableNotificationSender<>(sender, policy);
    }
}
