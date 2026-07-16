package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.email.EmailProvider;
import com.novacomp.notifications.validation.NotificationValidator;

public class EmailNotificationSender extends AbstractNotificationSender<EmailNotification> {

    private final EmailProvider provider;

    public EmailNotificationSender(EmailProvider provider,
                                    NotificationValidator<EmailNotification> validator,
                                    NotificationEventPublisher eventPublisher) {
        super(validator, eventPublisher);
        this.provider = provider;
    }

    @Override
    protected ProviderResponse doSend(EmailNotification notification) {
        return provider.send(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
