package com.novacomp.notifications.provider.sms;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.provider.ProviderResponse;

/** Puerto (Strategy) para proveedores de SMS. */
public interface SmsProvider {

    ProviderResponse send(SmsNotification notification);

    String getProviderName();
}
