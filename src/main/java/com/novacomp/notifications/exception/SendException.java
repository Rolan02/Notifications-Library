package com.novacomp.notifications.exception;

/**
 * Error ocurrido durante el envio real hacia el proveedor (SendGrid, Twilio,
 * FCM, Slack, etc). Representa fallas de infraestructura/proveedor, no de
 * los datos de la notificacion en si.
 */
public class SendException extends NotificationException {

    public SendException(String message) {
        super(message);
    }

    public SendException(String message, Throwable cause) {
        super(message, cause);
    }
}
