package com.novacomp.notifications.channel.push;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Notificacion push movil. El "recipient" hereda su significado como el
 * device token / FCM registration token; se agrega title y data payload.
 */
public final class PushNotification extends Notification {

    private final String title;
    private final Map<String, String> data;

    private PushNotification(Builder builder) {
        super(builder);
        this.title = Objects.requireNonNull(builder.title, "title es obligatorio para Push");
        this.data = builder.data == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(builder.data));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    public String getTitle() {
        return title;
    }

    /** Payload de datos custom (equivalente al campo "data" de FCM). */
    public Map<String, String> getData() {
        return data;
    }

    public static final class Builder extends Notification.Builder<Builder> {
        private String title;
        private Map<String, String> data;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder data(Map<String, String> data) {
            this.data = data;
            return this;
        }

        @Override
        public PushNotification build() {
            return new PushNotification(this);
        }
    }
}
