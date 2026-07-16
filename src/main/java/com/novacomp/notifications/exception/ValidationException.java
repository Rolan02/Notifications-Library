package com.novacomp.notifications.exception;

/**
 * Error de validacion de una notificacion ANTES de intentar enviarla
 * (ej. email con formato invalido, telefono invalido, campos requeridos
 * faltantes). Distinguirla de SendException permite al consumidor decidir,
 * por ejemplo, reintentar solo errores de envio pero no de validacion.
 */
public class ValidationException extends NotificationException {

    public ValidationException(String message) {
        super(message);
    }
}
