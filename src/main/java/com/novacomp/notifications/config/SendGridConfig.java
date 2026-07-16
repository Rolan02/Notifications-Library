package com.novacomp.notifications.config;

import java.util.Objects;

/**
 * Configuracion para el proveedor SendGrid (Email).
 * Ver: https://www.twilio.com/docs/sendgrid/api-reference/mail-send/mail-send
 * SendGrid espera: Authorization: Bearer {apiKey} + un remitente "from" verificado.
 */
public final class SendGridConfig {

    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    private SendGridConfig(Builder builder) {
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey es obligatorio");
        this.fromEmail = Objects.requireNonNull(builder.fromEmail, "fromEmail es obligatorio");
        this.fromName = builder.fromName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public static final class Builder {
        private String apiKey;
        private String fromEmail;
        private String fromName;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        public SendGridConfig build() {
            return new SendGridConfig(this);
        }
    }
}
