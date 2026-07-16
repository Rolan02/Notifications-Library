package com.novacomp.notifications.provider.email;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.config.SendGridConfig;
import com.novacomp.notifications.provider.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulacion del proveedor SendGrid.
 *
 * En una integracion real esto haria un POST a
 * https://api.sendgrid.com/v3/mail/send con headers:
 *   Authorization: Bearer {apiKey}
 *   Content-Type: application/json
 * y un body tipo:
 *   { "personalizations":[{"to":[{"email":"..."}]}],
 *     "from":{"email":"...", "name":"..."},
 *     "subject":"...",
 *     "content":[{"type":"text/plain","value":"..."}] }
 * y devolveria 202 Accepted con header X-Message-Id.
 */
public class SendGridEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailProvider.class);

    private final SendGridConfig config;

    public SendGridEmailProvider(SendGridConfig config) {
        this.config = config;
    }

    @Override
    public ProviderResponse send(EmailNotification notification) {
        log.info("[SendGrid] Enviando email desde '{}' a '{}' | subject='{}'",
                config.getFromEmail(), notification.getRecipient(), notification.getSubject());

        // --- Aqui iria la llamada HTTP real a la API de SendGrid ---
        String simulatedMessageId = "sg_" + UUID.randomUUID();
        log.info("[SendGrid] Respuesta simulada 202 Accepted, X-Message-Id={}", simulatedMessageId);
        return ProviderResponse.success(simulatedMessageId);
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }
}
