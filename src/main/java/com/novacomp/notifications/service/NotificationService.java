package com.novacomp.notifications.service;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.event.NotificationEventListener;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.exception.NotificationException;
import com.novacomp.notifications.sender.NotificationSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Punto de entrada unico (Facade) de la libreria. El codigo cliente solo
 * conoce esta clase y la jerarquia Notification; nunca referencia
 * directamente SendGrid/Twilio/FCM/Slack ni sus Senders concretos.
 *
 * Se construye exclusivamente via {@link NotificationServiceBuilder}.
 */
public final class NotificationService {

    private final Map<NotificationChannel, NotificationSender<? extends Notification>> senders;
    private final NotificationEventPublisher eventPublisher;

    NotificationService(Map<NotificationChannel, NotificationSender<? extends Notification>> senders,
                         NotificationEventPublisher eventPublisher) {
        this.senders = senders;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Envia una notificacion de forma sincrona. Misma llamada sin importar
     * el canal concreto de "notification".
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> NotificationResult send(T notification) {
        NotificationSender<T> sender = (NotificationSender<T>) getSenderOrThrow(notification.getChannel());
        return sender.send(notification);
    }

    @SuppressWarnings("unchecked")
    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        NotificationSender<T> sender = (NotificationSender<T>) getSenderOrThrow(notification.getChannel());
        return sender.sendAsync(notification);
    }

    /** Envia un lote de notificaciones (posiblemente de canales mezclados) en paralelo. */
    public List<NotificationResult> sendBatch(List<? extends Notification> notifications) {
        List<CompletableFuture<NotificationResult>> futures = new ArrayList<>(notifications.size());
        for (Notification notification : notifications) {
            futures.add(sendAsync(notification));
        }
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    /** Se suscribe a cambios de estado de cualquier notificacion enviada por este servicio. */
    public void subscribe(NotificationEventListener listener) {
        eventPublisher.subscribe(listener);
    }

    public boolean supports(NotificationChannel channel) {
        return senders.containsKey(channel);
    }

    private NotificationSender<? extends Notification> getSenderOrThrow(NotificationChannel channel) {
        NotificationSender<? extends Notification> sender = senders.get(channel);
        if (sender == null) {
            throw new NotificationException("No hay ningun sender registrado para el canal " + channel);
        }
        return sender;
    }
}
