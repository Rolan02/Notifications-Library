package com.novacomp.notifications.config;

import java.util.Objects;

/**
 * Configuracion para Slack Incoming Webhooks.
 * Ver: https://api.slack.com/messaging/webhooks
 * Slack Incoming Webhooks solo requieren una URL secreta por canal/workspace.
 */
public final class SlackConfig {

    private final String webhookUrl;

    private SlackConfig(Builder builder) {
        this.webhookUrl = Objects.requireNonNull(builder.webhookUrl, "webhookUrl es obligatorio");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public static final class Builder {
        private String webhookUrl;

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public SlackConfig build() {
            return new SlackConfig(this);
        }
    }
}
