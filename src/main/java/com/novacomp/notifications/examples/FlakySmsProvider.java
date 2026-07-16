package com.novacomp.notifications.examples;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.sms.SmsProvider;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proveedor de ejemplo que falla las primeras N veces y luego funciona,
 * usado SOLO en la demo para mostrar el decorator de reintentos en accion.
 * No forma parte de la libreria en si.
 */
public class FlakySmsProvider implements SmsProvider {

    private final SmsProvider delegate;
    private final int failuresBeforeSuccess;
    private final AtomicInteger calls = new AtomicInteger(0);

    public FlakySmsProvider(SmsProvider delegate, int failuresBeforeSuccess) {
        this.delegate = delegate;
        this.failuresBeforeSuccess = failuresBeforeSuccess;
    }

    @Override
    public ProviderResponse send(SmsNotification notification) {
        int callNumber = calls.incrementAndGet();
        if (callNumber <= failuresBeforeSuccess) {
            return ProviderResponse.failure("Timeout simulado de red (intento " + callNumber + ")");
        }
        return delegate.send(notification);
    }

    @Override
    public String getProviderName() {
        return "Twilio (flaky-demo)";
    }
}
