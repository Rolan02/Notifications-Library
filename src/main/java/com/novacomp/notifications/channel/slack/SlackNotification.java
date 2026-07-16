package com.novacomp.notifications.channel.slack;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;

/**
 * Notificacion para Slack (canal opcional). El "recipient" representa el
 * canal/webhook destino (ej. "#alertas-produccion" o un webhook URL logico).
 */
public final class SlackNotification extends Notification {

    private final String username;
    private final String iconEmoji;

    private SlackNotification(Builder builder) {
        super(builder);
        this.username = builder.username != null ? builder.username : "Notifications-Lib";
        this.iconEmoji = builder.iconEmoji;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }

    public String getUsername() {
        return username;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public static final class Builder extends Notification.Builder<Builder> {
        private String username;
        private String iconEmoji;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder iconEmoji(String iconEmoji) {
            this.iconEmoji = iconEmoji;
            return this;
        }

        @Override
        public SlackNotification build() {
            return new SlackNotification(this);
        }
    }
}
