package com.novacomp.notifications.validation;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.exception.ValidationException;

/**
 * Estrategia de validacion para un tipo de Notification.
 * Cada canal puede tener su propio validador (o componer varios).
 *
 * @param <T> subtipo concreto de Notification que este validador entiende
 */
@FunctionalInterface
public interface NotificationValidator<T extends Notification> {

    /**
     * @throws ValidationException si la notificacion no es valida
     */
    void validate(T notification) throws ValidationException;
}
