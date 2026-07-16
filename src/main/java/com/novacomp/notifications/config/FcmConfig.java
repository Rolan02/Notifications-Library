package com.novacomp.notifications.config;

import java.util.Objects;

/**
 * Configuracion para Firebase Cloud Messaging (Push).
 * Ver: https://firebase.google.com/docs/cloud-messaging/send-message
 * FCM v1 usa OAuth2 con un service account (projectId + credenciales JSON);
 * aqui se modela de forma simplificada con projectId + una credencial ya resuelta.
 */
public final class FcmConfig {

    private final String projectId;
    private final String serverKey;

    private FcmConfig(Builder builder) {
        this.projectId = Objects.requireNonNull(builder.projectId, "projectId es obligatorio");
        this.serverKey = Objects.requireNonNull(builder.serverKey, "serverKey/credencial es obligatorio");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getServerKey() {
        return serverKey;
    }

    public static final class Builder {
        private String projectId;
        private String serverKey;

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder serverKey(String serverKey) {
            this.serverKey = serverKey;
            return this;
        }

        public FcmConfig build() {
            return new FcmConfig(this);
        }
    }
}
