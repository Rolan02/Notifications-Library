package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.push.PushProvider;
import com.novacomp.notifications.validation.NotificationValidator;

public class PushNotificationSender extends AbstractNotificationSender<PushNotification> {

    private final PushProvider provider;

    public PushNotificationSender(PushProvider provider,
                                   NotificationValidator<PushNotification> validator,
                                   NotificationEventPublisher eventPublisher) {
        super(validator, eventPublisher);
        this.provider = provider;
    }

    @Override
    protected ProviderResponse doSend(PushNotification notification) {
        return provider.send(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }
}
