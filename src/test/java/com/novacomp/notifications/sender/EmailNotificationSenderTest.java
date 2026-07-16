package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.core.NotificationStatus;
import com.novacomp.notifications.event.NotificationEventPublisher;
import com.novacomp.notifications.exception.ValidationException;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.email.EmailProvider;
import com.novacomp.notifications.validation.EmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private EmailProvider provider;

    @Test
    void enviaCorrectamenteCuandoElProveedorRespondeExito() {
        EmailNotificationSender sender =
                new EmailNotificationSender(provider, new EmailValidator(), new NotificationEventPublisher());

        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@dominio.com")
                .subject("Hola")
                .message("Cuerpo")
                .build();

        when(provider.send(email)).thenReturn(ProviderResponse.success("provider-id-123"));

        NotificationResult result = sender.send(email);

        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertEquals("provider-id-123", result.getProviderMessageId());
        verify(provider).send(email);
    }

    @Test
    void devuelveResultadoFallidoSinLanzarExcepcionCuandoElProveedorFalla() {
        EmailNotificationSender sender =
                new EmailNotificationSender(provider, new EmailValidator(), new NotificationEventPublisher());

        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@dominio.com")
                .subject("Hola")
                .message("Cuerpo")
                .build();

        when(provider.send(email)).thenReturn(ProviderResponse.failure("timeout de proveedor"));

        NotificationResult result = sender.send(email);

        assertEquals(NotificationStatus.FAILED, result.getStatus());
        assertEquals("timeout de proveedor", result.getErrorMessage());
    }

    @Test
    void propagaValidationExceptionSinLlamarAlProveedor() {
        EmailNotificationSender sender =
                new EmailNotificationSender(provider, new EmailValidator(), new NotificationEventPublisher());

        EmailNotification emailInvalido = EmailNotification.builder()
                .recipient("no-es-email")
                .subject("Hola")
                .message("Cuerpo")
                .build();

        assertThrows(ValidationException.class, () -> sender.send(emailInvalido));
    }
}
