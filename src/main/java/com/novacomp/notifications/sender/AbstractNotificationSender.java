package com.novacomp.notifications.sender;

import com.novacomp.notifications.core.Notification;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.core.NotificationStatus;
import com.novacomp.notifications.event.NotificationEvent;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.validation.NotificationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Implementacion plantilla (patron Template Method) compartida por todos los
 * senders concretos: valida -> delega el envio real al proveedor -> traduce
 * la respuesta a NotificationResult -> publica un evento de estado.
 *
 * Cada canal solo necesita implementar {@link #doSend(Notification)}.
 */
public abstract class AbstractNotificationSender<T extends Notification> implements NotificationSender<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractNotificationSender.class);

    private final NotificationValidator<T> validator;
    private final NotificationEventPublisher eventPublisher;
    private final Executor executor;

    protected AbstractNotificationSender(NotificationValidator<T> validator,
                                          NotificationEventPublisher eventPublisher) {
        this(validator, eventPublisher, ForkJoinPool.commonPool());
    }

    protected AbstractNotificationSender(NotificationValidator<T> validator,
                                          NotificationEventPublisher eventPublisher,
                                          Executor executor) {
        this.validator = validator;
        this.eventPublisher = eventPublisher;
        this.executor = executor;
    }

    /** Llama al proveedor concreto (SendGrid, Twilio, FCM, Slack, ...). */
    protected abstract ProviderResponse doSend(T notification);

    @Override
    public final NotificationResult send(T notification) {
        // Errores de validacion se propagan: son responsabilidad de quien llama.
        validator.validate(notification);

        NotificationResult result;
        try {
            ProviderResponse response = doSend(notification);
            result = NotificationResult.builder()
                    .notificationId(notification.getId())
                    .channel(getChannel())
                    .status(response.isSuccess() ? NotificationStatus.SENT : NotificationStatus.FAILED)
                    .providerMessageId(response.getProviderMessageId())
                    .errorMessage(response.getErrorMessage())
                    .attempts(1)
                    .build();
        } catch (RuntimeException providerFailure) {
            // Error de infraestructura/proveedor: no se propaga, se refleja en el resultado.
            log.warn("Fallo de envio en canal {} para notificacion {}: {}",
                    getChannel(), notification.getId(), providerFailure.getMessage());
            result = NotificationResult.builder()
                    .notificationId(notification.getId())
                    .channel(getChannel())
                    .status(NotificationStatus.FAILED)
                    .errorMessage(providerFailure.getMessage())
                    .attempts(1)
                    .build();
        }

        eventPublisher.publish(NotificationEvent.fromResult(result));
        return result;
    }

    @Override
    public final CompletableFuture<NotificationResult> sendAsync(T notification) {
        return CompletableFuture.supplyAsync(() -> send(notification), executor);
    }
}
