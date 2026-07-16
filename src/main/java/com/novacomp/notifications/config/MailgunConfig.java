package com.novacomp.notifications.config;

import java.util.Objects;

/**
 * Configuracion para el proveedor Mailgun (Email), alternativa a SendGrid.
 * Ver: https://documentation.mailgun.com/en/latest/api-sending.html
 * Mailgun requiere apiKey + domain (ej. "mg.miempresa.com") + remitente.
 */
public final class MailgunConfig {

    private final String apiKey;
    private final String domain;
    private final String fromEmail;

    private MailgunConfig(Builder builder) {
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey es obligatorio");
        this.domain = Objects.requireNonNull(builder.domain, "domain es obligatorio");
        this.fromEmail = Objects.requireNonNull(builder.fromEmail, "fromEmail es obligatorio");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDomain() {
        return domain;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public static final class Builder {
        private String apiKey;
        private String domain;
        private String fromEmail;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        public MailgunConfig build() {
            return new MailgunConfig(this);
        }
    }
}
