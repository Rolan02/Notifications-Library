package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.slack.SlackProvider;
import com.novacomp.notifications.validation.NotificationValidator;

public class SlackNotificationSender extends AbstractNotificationSender<SlackNotification> {

    private final SlackProvider provider;

    public SlackNotificationSender(SlackProvider provider,
                                    NotificationValidator<SlackNotification> validator,
                                    NotificationEventPublisher eventPublisher) {
        super(validator, eventPublisher);
        this.provider = provider;
    }

    @Override
    protected ProviderResponse doSend(SlackNotification notification) {
        return provider.send(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }
}
