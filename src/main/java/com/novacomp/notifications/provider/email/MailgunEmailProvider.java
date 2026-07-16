package com.novacomp.notifications.provider.email;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.config.MailgunConfig;
import com.novacomp.notifications.provider.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulacion del proveedor Mailgun.
 *
 * En una integracion real esto haria un POST (form-urlencoded) a
 * https://api.mailgun.net/v3/{domain}/messages con Basic Auth
 * ("api", apiKey) y campos from/to/subject/text.
 * Mailgun responde 200 OK con {"id": "...", "message": "Queued. Thank you."}
 */
public class MailgunEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(MailgunEmailProvider.class);

    private final MailgunConfig config;

    public MailgunEmailProvider(MailgunConfig config) {
        this.config = config;
    }

    @Override
    public ProviderResponse send(EmailNotification notification) {
        log.info("[Mailgun] Enviando email via dominio '{}' desde '{}' a '{}'",
                config.getDomain(), config.getFromEmail(), notification.getRecipient());

        // --- Aqui iria la llamada HTTP real a la API de Mailgun ---
        String simulatedMessageId = "<" + UUID.randomUUID() + "@" + config.getDomain() + ">";
        log.info("[Mailgun] Respuesta simulada 200 OK, id={}", simulatedMessageId);
        return ProviderResponse.success(simulatedMessageId);
    }

    @Override
    public String getProviderName() {
        return "Mailgun";
    }
}
