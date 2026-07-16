package com.novacomp.notifications.sender;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.config.RetryPolicy;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.core.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryableNotificationSenderTest {

    @Mock
    private NotificationSender<SmsNotification> delegate;

    private final SmsNotification sms = SmsNotification.builder()
            .recipient("+51987654321")
            .message("Hola")
            .build();

    @Test
    void reintentaHastaTenerExitoYReportaLosIntentosUsados() {
        NotificationResult fallo = NotificationResult.builder()
                .notificationId(sms.getId())
                .channel(NotificationChannel.SMS)
                .status(NotificationStatus.FAILED)
                .errorMessage("timeout")
                .attempts(1)
                .build();
        NotificationResult exito = NotificationResult.builder()
                .notificationId(sms.getId())
                .channel(NotificationChannel.SMS)
                .status(NotificationStatus.SENT)
                .providerMessageId("sid-123")
                .attempts(1)
                .build();

        when(delegate.send(sms)).thenReturn(fallo, fallo, exito);

        RetryableNotificationSender<SmsNotification> retrySender = new RetryableNotificationSender<>(
                delegate,
                RetryPolicy.builder().maxAttempts(3).initialDelayMillis(1).backoffMultiplier(1.0).build());

        NotificationResult result = retrySender.send(sms);

        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertEquals(3, result.getAttempts());
        verify(delegate, times(3)).send(sms);
    }

    @Test
    void noExcedeElMaximoDeIntentosSiSiempreFalla() {
        NotificationResult fallo = NotificationResult.builder()
                .notificationId(sms.getId())
                .channel(NotificationChannel.SMS)
                .status(NotificationStatus.FAILED)
                .errorMessage("proveedor caido")
                .attempts(1)
                .build();

        when(delegate.send(sms)).thenReturn(fallo);

        RetryableNotificationSender<SmsNotification> retrySender = new RetryableNotificationSender<>(
                delegate,
                RetryPolicy.builder().maxAttempts(2).initialDelayMillis(1).backoffMultiplier(1.0).build());

        NotificationResult result = retrySender.send(sms);

        assertEquals(NotificationStatus.FAILED, result.getStatus());
        assertEquals(2, result.getAttempts());
        verify(delegate, times(2)).send(sms);
    }
}
