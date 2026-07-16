package com.novacomp.notifications.provider.push;

import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.provider.ProviderResponse;

/** Puerto (Strategy) para proveedores de Push. */
public interface PushProvider {

    ProviderResponse send(PushNotification notification);

    String getProviderName();
}
