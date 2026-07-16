package com.novacomp.notifications.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publisher simple en memoria (patron Observer / Pub-Sub). Los
 * NotificationSender publican eventos aqui; cualquier parte de la
 * aplicacion consumidora puede suscribirse sin acoplarse a los senders.
 *
 * Un listener que lanza una excepcion no debe tumbar el envio de la
 * notificacion en si, por eso cada notificacion a listeners se aisla.
 */
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final List<NotificationEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(NotificationEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(NotificationEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(NotificationEvent event) {
        for (NotificationEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                log.warn("Un listener de eventos fallo procesando {}: {}", event, e.getMessage());
            }
        }
    }
}
