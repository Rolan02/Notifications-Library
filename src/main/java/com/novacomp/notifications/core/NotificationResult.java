package com.novacomp.notifications.core;

import java.time.Instant;

/**
 * Resultado uniforme de un intento de envio, sin importar el canal o proveedor.
 * Se usa en lugar de propagar excepciones para el "camino feliz" de negocio,
 * reservando las excepciones para errores de validacion / configuracion
 * (ver com.novacomp.notifications.exception).
 */
public final class NotificationResult {

    private final String notificationId;
    private final NotificationChannel channel;
    private final NotificationStatus status;
    private final String providerMessageId;
    private final String errorMessage;
    private final int attempts;
    private final Instant timestamp;

    private NotificationResult(Builder builder) {
        this.notificationId = builder.notificationId;
        this.channel = builder.channel;
        this.status = builder.status;
        this.providerMessageId = builder.providerMessageId;
        this.errorMessage = builder.errorMessage;
        this.attempts = builder.attempts;
        this.timestamp = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() {
        return status == NotificationStatus.SENT;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /** Crea una copia de este resultado con un numero de intentos distinto (usado al reintentar). */
    public NotificationResult withAttempts(int newAttempts) {
        return NotificationResult.builder()
                .notificationId(this.notificationId)
                .channel(this.channel)
                .status(this.status)
                .providerMessageId(this.providerMessageId)
                .errorMessage(this.errorMessage)
                .attempts(newAttempts)
                .build();
    }

    @Override
    public String toString() {
        return "NotificationResult{" +
                "notificationId='" + notificationId + '\'' +
                ", channel=" + channel +
                ", status=" + status +
                ", providerMessageId='" + providerMessageId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", attempts=" + attempts +
                '}';
    }

    public static final class Builder {
        private String notificationId;
        private NotificationChannel channel;
        private NotificationStatus status;
        private String providerMessageId;
        private String errorMessage;
        private int attempts = 1;

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder status(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public Builder providerMessageId(String providerMessageId) {
            this.providerMessageId = providerMessageId;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder attempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public NotificationResult build() {
            return new NotificationResult(this);
        }
    }
}
