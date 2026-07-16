package com.novacomp.notifications.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstraccion comun a todos los canales de notificacion.
 *
 * <p>Cada canal concreto (EmailNotification, SmsNotification, PushNotification,
 * SlackNotification, ...) extiende esta clase y agrega los campos propios de su
 * canal (ej. Email tiene "subject", SMS no). El resto de la libreria (senders,
 * validadores, servicio) trabaja contra esta clase base o sus subtipos concretos,
 * nunca contra un canal "generico" con campos opcionales para todo -- eso
 * violaria Interface Segregation y ensuciaria el modelo.</p>
 */
public abstract class Notification {

    private final String id;
    private final String recipient;
    private final String message;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    protected Notification(Builder<?> builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.recipient = Objects.requireNonNull(builder.recipient, "recipient es obligatorio");
        this.message = Objects.requireNonNull(builder.message, "message es obligatorio");
        this.createdAt = Instant.now();
        this.metadata = builder.metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }

    public abstract NotificationChannel getChannel();

    public String getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Builder generico auto-referenciado (patron "curiously recurring generic
     * builder") que permite que cada subclase concreta tenga su propio Builder
     * fluido (EmailNotification.builder()...) sin duplicar recipient/message.
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> {
        private String id;
        private String recipient;
        private String message;
        private Map<String, String> metadata;

        public B id(String id) {
            this.id = id;
            return (B) this;
        }

        public B recipient(String recipient) {
            this.recipient = recipient;
            return (B) this;
        }

        public B message(String message) {
            this.message = message;
            return (B) this;
        }

        public B metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return (B) this;
        }

        public abstract Notification build();
    }
}
