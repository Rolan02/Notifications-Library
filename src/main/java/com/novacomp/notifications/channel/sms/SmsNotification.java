package com.novacomp.notifications.channel.sms;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;

/** Notificacion de canal SMS: solo destinatario (telefono E.164) y mensaje de texto. */
public final class SmsNotification extends Notification {

    private final String senderId;

    private SmsNotification(Builder builder) {
        super(builder);
        this.senderId = builder.senderId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    /** Alias/remitente alfanumerico opcional (soportado por Twilio en varios paises). */
    public String getSenderId() {
        return senderId;
    }

    public static final class Builder extends Notification.Builder<Builder> {
        private String senderId;

        public Builder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        @Override
        public SmsNotification build() {
            return new SmsNotification(this);
        }
    }
}
