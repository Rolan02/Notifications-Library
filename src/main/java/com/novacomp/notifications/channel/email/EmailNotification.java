package com.novacomp.notifications.channel.email;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Notificacion de canal Email: agrega subject, html opcional y adjuntos. */
public final class EmailNotification extends Notification {

    private final String subject;
    private final String htmlBody;
    private final List<String> attachmentNames;

    private EmailNotification(Builder builder) {
        super(builder);
        this.subject = Objects.requireNonNull(builder.subject, "subject es obligatorio para Email");
        this.htmlBody = builder.htmlBody;
        this.attachmentNames = builder.attachmentNames == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.attachmentNames));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    public String getSubject() {
        return subject;
    }

    /** Cuerpo HTML opcional; si es null, el proveedor debe usar getMessage() como texto plano. */
    public String getHtmlBody() {
        return htmlBody;
    }

    public List<String> getAttachmentNames() {
        return attachmentNames;
    }

    public static final class Builder extends Notification.Builder<Builder> {
        private String subject;
        private String htmlBody;
        private List<String> attachmentNames;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public Builder attachmentNames(List<String> attachmentNames) {
            this.attachmentNames = attachmentNames;
            return this;
        }

        @Override
        public EmailNotification build() {
            return new EmailNotification(this);
        }
    }
}
