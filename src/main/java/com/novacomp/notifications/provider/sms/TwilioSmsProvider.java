package com.novacomp.notifications.provider.sms;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.config.TwilioConfig;
import com.novacomp.notifications.provider.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulacion del proveedor Twilio.
 *
 * En una integracion real esto haria un POST a
 * https://api.twilio.com/2010-04-01/Accounts/{AccountSid}/Messages.json
 * con Basic Auth (accountSid, authToken) y form params From/To/Body.
 * Twilio responde 201 Created con un "sid" (ej. SMxxxxxxxx...) y un "status"
 * (queued, sent, delivered, failed, undelivered).
 */
public class TwilioSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsProvider.class);

    private final TwilioConfig config;

    public TwilioSmsProvider(TwilioConfig config) {
        this.config = config;
    }

    @Override
    public ProviderResponse send(SmsNotification notification) {
        log.info("[Twilio] Enviando SMS desde '{}' a '{}'", config.getFromNumber(), notification.getRecipient());

        // --- Aqui iria la llamada HTTP real a la API de Twilio ---
        String simulatedSid = "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        log.info("[Twilio] Respuesta simulada 201 Created, sid={}, status=queued", simulatedSid);
        return ProviderResponse.success(simulatedSid);
    }

    @Override
    public String getProviderName() {
        return "Twilio";
    }
}
