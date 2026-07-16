package com.novacomp.notifications.config;

import java.util.Objects;

/**
 * Configuracion para Twilio (SMS).
 * Ver: https://www.twilio.com/docs/sms/send-messages
 * Twilio requiere accountSid + authToken (Basic Auth) + numero remitente "from".
 */
public final class TwilioConfig {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    private TwilioConfig(Builder builder) {
        this.accountSid = Objects.requireNonNull(builder.accountSid, "accountSid es obligatorio");
        this.authToken = Objects.requireNonNull(builder.authToken, "authToken es obligatorio");
        this.fromNumber = Objects.requireNonNull(builder.fromNumber, "fromNumber es obligatorio");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public static final class Builder {
        private String accountSid;
        private String authToken;
        private String fromNumber;

        public Builder accountSid(String accountSid) {
            this.accountSid = accountSid;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Builder fromNumber(String fromNumber) {
            this.fromNumber = fromNumber;
            return this;
        }

        public TwilioConfig build() {
            return new TwilioConfig(this);
        }
    }
}
