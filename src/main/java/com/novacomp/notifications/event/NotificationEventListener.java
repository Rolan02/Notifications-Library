package com.novacomp.notifications.event;

/**
 * Suscriptor (patron Observer) que quiere enterarse de cambios de estado de
 * notificaciones -- por ejemplo, para actualizar un dashboard, escribir
 * metricas, o disparar alertas cuando algo falla repetidamente.
 */
@FunctionalInterface
public interface NotificationEventListener {

    void onEvent(NotificationEvent event);
}
