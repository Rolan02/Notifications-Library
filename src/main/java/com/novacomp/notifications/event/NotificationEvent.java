package com.novacomp.notifications.event;

import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.core.NotificationStatus;

import java.time.Instant;

/** Evento inmutable emitido cada vez que cambia el estado de una notificacion. */
public final class NotificationEvent {

    private final String notificationId;
    private final NotificationChannel channel;
    private final NotificationStatus status;
    private final String detail;
    private final Instant occurredAt;

    public NotificationEvent(String notificationId, NotificationChannel channel,
                              NotificationStatus status, String detail) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
        this.detail = detail;
        this.occurredAt = Instant.now();
    }

    public static NotificationEvent fromResult(NotificationResult result) {
        return new NotificationEvent(
                result.getNotificationId(),
                result.getChannel(),
                result.getStatus(),
                result.isSuccess() ? result.getProviderMessageId() : result.getErrorMessage());
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

    public String getDetail() {
        return detail;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "NotificationEvent{" +
                "notificationId='" + notificationId + '\'' +
                ", channel=" + channel +
                ", status=" + status +
                ", detail='" + detail + '\'' +
                '}';
    }
}
