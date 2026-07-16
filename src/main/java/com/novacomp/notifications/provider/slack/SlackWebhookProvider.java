package com.novacomp.notifications.provider.slack;

import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.config.SlackConfig;
import com.novacomp.notifications.provider.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulacion de un Slack Incoming Webhook.
 *
 * En una integracion real esto haria un POST a la webhookUrl con body:
 *   { "text": "...", "username": "...", "icon_emoji": ":robot_face:" }
 * Slack responde 200 OK con el texto plano "ok".
 */
public class SlackWebhookProvider implements SlackProvider {

    private static final Logger log = LoggerFactory.getLogger(SlackWebhookProvider.class);

    private final SlackConfig config;

    public SlackWebhookProvider(SlackConfig config) {
        this.config = config;
    }

    @Override
    public ProviderResponse send(SlackNotification notification) {
        log.info("[Slack] Enviando mensaje a '{}' como '{}'", notification.getRecipient(), notification.getUsername());

        // --- Aqui iria el POST real al webhookUrl configurado en config.getWebhookUrl() ---
        String simulatedId = "slack_" + UUID.randomUUID();
        log.info("[Slack] Respuesta simulada 200 OK ('ok'), trackingId={}", simulatedId);
        return ProviderResponse.success(simulatedId);
    }

    @Override
    public String getProviderName() {
        return "Slack";
    }
}
