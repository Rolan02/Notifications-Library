package com.novacomp.notifications.core;

/** Estado del ciclo de vida de una notificacion. */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    RETRYING
}
