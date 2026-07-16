package com.novacomp.notifications.sender;

import com.novacomp.notifications.config.RetryPolicy;
import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Decorator (patron Decorator) que agrega reintentos con backoff a
 * CUALQUIER NotificationSender existente, sin modificar su codigo
 * (Open/Closed). Solo reintenta fallos de envio; si la validacion falla
 * (ValidationException), se propaga inmediatamente en el primer intento
 * ya que reintentar datos invalidos no tiene sentido.
 *
 * @param <T> subtipo de Notification manejado por el sender decorado
 */
public class RetryableNotificationSender<T extends Notification> implements NotificationSender<T> {

    private static final Logger log = LoggerFactory.getLogger(RetryableNotificationSender.class);

    private final NotificationSender<T> delegate;
    private final RetryPolicy retryPolicy;
    private final Executor executor;

    public RetryableNotificationSender(NotificationSender<T> delegate, RetryPolicy retryPolicy) {
        this(delegate, retryPolicy, ForkJoinPool.commonPool());
    }

    public RetryableNotificationSender(NotificationSender<T> delegate, RetryPolicy retryPolicy, Executor executor) {
        this.delegate = delegate;
        this.retryPolicy = retryPolicy;
        this.executor = executor;
    }

    @Override
    public NotificationResult send(T notification) {
        NotificationResult lastResult = null;
        int maxAttempts = retryPolicy.getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            lastResult = delegate.send(notification);
            if (lastResult.isSuccess()) {
                return attempt == 1 ? lastResult : lastResult.withAttempts(attempt);
            }

            if (attempt < maxAttempts) {
                long delay = retryPolicy.delayForAttempt(attempt);
                log.info("Intento {}/{} fallo para notificacion {} ({}). Reintentando en {}ms...",
                        attempt, maxAttempts, notification.getId(), lastResult.getErrorMessage(), delay);
                sleepQuietly(delay);
            }
        }
        return lastResult.withAttempts(maxAttempts);
    }

    @Override
    public CompletableFuture<NotificationResult> sendAsync(T notification) {
        return CompletableFuture.supplyAsync(() -> send(notification), executor);
    }

    @Override
    public NotificationChannel getChannel() {
        return delegate.getChannel();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
