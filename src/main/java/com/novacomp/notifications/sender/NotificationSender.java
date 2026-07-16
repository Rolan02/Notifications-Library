package com.novacomp.notifications.sender;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.exception.ValidationException;

import java.util.concurrent.CompletableFuture;

/**
 * Contrato unico que deben cumplir todos los canales (Strategy). El codigo
 * cliente programa contra esta interfaz, nunca contra SendGridEmailProvider,
 * TwilioSmsProvider, etc. directamente -- eso es lo que permite cambiar de
 * proveedor sin tocar el codigo que consume la libreria (Dependency Inversion).
 *
 * @param <T> subtipo de Notification que este sender sabe procesar
 */
public interface NotificationSender<T extends Notification> {

    /**
     * Envia la notificacion de forma sincrona.
     *
     * @throws ValidationException si la notificacion no pasa las validaciones
     *         de negocio (esto SI se propaga, ya que es un error de uso).
     *         Los errores de infraestructura/proveedor NO se propagan como
     *         excepcion: se reflejan en el NotificationResult devuelto con
     *         status FAILED, para que enviar en lote no requiera try/catch
     *         por cada item.
     */
    NotificationResult send(T notification);

    /** Version no bloqueante de {@link #send(Notification)}. */
    CompletableFuture<NotificationResult> sendAsync(T notification);

    NotificationChannel getChannel();
}
