package com.novacomp.notifications.service;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.core.NotificationStatus;
import com.novacomp.notifications.exception.NotificationException;
import com.novacomp.notifications.provider.ProviderResponse;
import com.novacomp.notifications.provider.email.EmailProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailProvider emailProvider;

    @Test
    void enviaAlCanalCorrectoSegunElTipoDeNotificacion() {
        when(emailProvider.send(any())).thenReturn(ProviderResponse.success("id-1"));

        NotificationService service = NotificationServiceBuilder.create()
                .registerEmailSender(emailProvider)
                .build();

        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@dominio.com")
                .subject("Asunto")
                .message("Cuerpo")
                .build();

        NotificationResult result = service.send(email);

        assertEquals(NotificationStatus.SENT, result.getStatus());
    }

    @Test
    void lanzaExcepcionSiNoHaySenderRegistradoParaElCanal() {
        NotificationService service = NotificationServiceBuilder.create()
                .registerEmailSender(emailProvider)
                .build();

        SlackNotification slack = SlackNotification.builder()
                .recipient("#canal")
                .message("hola")
                .build();

        assertThrows(NotificationException.class, () -> service.send(slack));
    }

    @Test
    void notificaAlListenerSuscritoCuandoSeEnviaUnaNotificacion() {
        when(emailProvider.send(any())).thenReturn(ProviderResponse.success("id-2"));

        NotificationService service = NotificationServiceBuilder.create()
                .registerEmailSender(emailProvider)
                .build();

        AtomicReference<NotificationStatus> ultimoEstado = new AtomicReference<>();
        service.subscribe(event -> ultimoEstado.set(event.getStatus()));

        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@dominio.com")
                .subject("Asunto")
                .message("Cuerpo")
                .build();

        service.send(email);

        assertEquals(NotificationStatus.SENT, ultimoEstado.get());
    }

    @Test
    void enviaUnLoteDeNotificaciones() {
        when(emailProvider.send(any())).thenReturn(ProviderResponse.success("id-3"));

        NotificationService service = NotificationServiceBuilder.create()
                .registerEmailSender(emailProvider)
                .build();

        EmailNotification email1 = EmailNotification.builder()
                .recipient("a@dominio.com").subject("A").message("msg").build();
        EmailNotification email2 = EmailNotification.builder()
                .recipient("b@dominio.com").subject("B").message("msg").build();

        List<NotificationResult> results = service.sendBatch(List.of(email1, email2));

        assertEquals(2, results.size());
        results.forEach(r -> assertEquals(NotificationStatus.SENT, r.getStatus()));
    }
}
