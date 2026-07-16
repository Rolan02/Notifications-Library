package com.novacomp.notifications.exception;

/**
 * Excepcion base (unchecked) de la libreria. Se usa unchecked a proposito:
 * obligar a un try/catch de checked exceptions en cada send() de cada canal
 * es ruido para el consumidor de la API; en su lugar exponemos jerarquia
 * clara (Validation vs Send) para que quien la use decida que capturar.
 */
public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
