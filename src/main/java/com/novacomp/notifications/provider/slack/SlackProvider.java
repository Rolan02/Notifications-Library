package com.novacomp.notifications.provider.slack;

import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.provider.ProviderResponse;

/** Puerto (Strategy) para proveedores de Slack. */
public interface SlackProvider {

    ProviderResponse send(SlackNotification notification);

    String getProviderName();
}
