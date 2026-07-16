package com.novacomp.notifications.core;

/**
 * Canales de notificacion soportados por la libreria.
 * Agregar un nuevo canal NO requiere modificar esta libreria (Open/Closed):
 * el enum solo se usa como metadata/routing, la logica real vive en
 * implementaciones de Notification + NotificationSender.
 */
public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    SLACK
}
