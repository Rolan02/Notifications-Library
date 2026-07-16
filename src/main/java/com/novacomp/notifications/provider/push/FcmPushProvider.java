package com.novacomp.notifications.provider.push;

import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.config.FcmConfig;
import com.novacomp.notifications.provider.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulacion del proveedor Firebase Cloud Messaging (FCM) v1.
 *
 * En una integracion real esto haria un POST a
 * https://fcm.googleapis.com/v1/projects/{projectId}/messages:send
 * con OAuth2 Bearer token y body:
 *   { "message": { "token": "...", "notification": {"title":"...","body":"..."},
 *                  "data": {...} } }
 * FCM responde 200 OK con { "name": "projects/{p}/messages/{id}" }.
 */
public class FcmPushProvider implements PushProvider {

    private static final Logger log = LoggerFactory.getLogger(FcmPushProvider.class);

    private final FcmConfig config;

    public FcmPushProvider(FcmConfig config) {
        this.config = config;
    }

    @Override
    public ProviderResponse send(PushNotification notification) {
        log.info("[FCM] Enviando push (proyecto '{}') a token '{}' | title='{}'",
                config.getProjectId(), notification.getRecipient(), notification.getTitle());

        // --- Aqui iria la llamada HTTP real a la API de FCM ---
        String simulatedName = "projects/" + config.getProjectId() + "/messages/" + UUID.randomUUID();
        log.info("[FCM] Respuesta simulada 200 OK, name={}", simulatedName);
        return ProviderResponse.success(simulatedName);
    }

    @Override
    public String getProviderName() {
        return "FCM";
    }
}
