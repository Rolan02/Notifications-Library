package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.sms.SmsProvider;
import com.novacomp.notifications.validation.NotificationValidator;

public class SmsNotificationSender extends AbstractNotificationSender<SmsNotification> {

    private final SmsProvider provider;

    public SmsNotificationSender(SmsProvider provider,
                                  NotificationValidator<SmsNotification> validator,
                                  NotificationEventPublisher eventPublisher) {
        super(validator, eventPublisher);
        this.provider = provider;
    }

    @Override
    protected ProviderResponse doSend(SmsNotification notification) {
        return provider.send(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
