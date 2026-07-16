package com.novacomp.notifications.examples;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.config.FcmConfig;
import com.novacomp.notifications.config.RetryPolicy;
import com.novacomp.notifications.config.SendGridConfig;
import com.novacomp.notifications.config.SlackConfig;
import com.novacomp.notifications.config.TwilioConfig;
import com.novacomp.notifications.core.NotificationChannel;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.exception.ValidationException;
import com.novacomp.notifications.provider.push.FcmPushProvider;
import com.novacomp.notifications.provider.slack.SlackWebhookProvider;
import com.novacomp.notifications.provider.sms.TwilioSmsProvider;
import com.novacomp.notifications.provider.email.SendGridEmailProvider;
import com.novacomp.notifications.service.NotificationService;
import com.novacomp.notifications.service.NotificationServiceBuilder;
import com.novacomp.notifications.template.NotificationTemplate;
import com.novacomp.notifications.template.TemplateEngine;
import com.novacomp.notifications.template.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Clase ejecutable con ejemplos de uso de la libreria de notificaciones.
 * Pensada como material de referencia para quien la consuma, y como demo
 * para el Dockerfile (`java -jar ... NotificationExamples`).
 */
public final class NotificationExamples {

    private static final Logger log = LoggerFactory.getLogger(NotificationExamples.class);

    private NotificationExamples() {
    }

    public static void main(String[] args) throws Exception {
        NotificationService service = buildService();

        // Pub/Sub: cualquier parte de la app puede escuchar cambios de estado
        // sin acoplarse a los senders concretos.
        service.subscribe(event ->
                log.info(">> [EVENTO] canal={} estado={} detalle={}",
                        event.getChannel(), event.getStatus(), event.getDetail()));

        demoEnvioSincrono(service);
        demoValidacionFallida(service);
        demoTemplates(service);
        demoAsincronoYLote(service);

        // Se da tiempo a que terminen los envios asincronos antes de salir.
        Thread.sleep(500);
    }

    private static NotificationService buildService() {
        SendGridConfig sendGridConfig = SendGridConfig.builder()
                .apiKey("SG.fake-api-key")
                .fromEmail("no-reply@infinance-xp.pe")
                .fromName("InFinance XP")
                .build();

        TwilioConfig twilioConfig = TwilioConfig.builder()
                .accountSid("ACfake0000000000000000000000000")
                .authToken("fake-auth-token")
                .fromNumber("+15005550006")
                .build();

        FcmConfig fcmConfig = FcmConfig.builder()
                .projectId("infinance-xp-app")
                .serverKey("fake-fcm-server-key")
                .build();

        SlackConfig slackConfig = SlackConfig.builder()
                .webhookUrl("https://hooks.slack.com/services/FAKE/WEBHOOK/URL")
                .build();

        // Proveedor SMS "inestable" solo para ilustrar el decorator de reintentos.
        FlakySmsProvider flakyTwilio = new FlakySmsProvider(new TwilioSmsProvider(twilioConfig), 2);

        return NotificationServiceBuilder.create()
                .registerEmailSender(new SendGridEmailProvider(sendGridConfig))
                .registerSmsSender(flakyTwilio)
                .registerPushSender(new FcmPushProvider(fcmConfig))
                .registerSlackSender(new SlackWebhookProvider(slackConfig))
                .withRetry(NotificationChannel.SMS, RetryPolicy.builder()
                        .maxAttempts(3)
                        .initialDelayMillis(100)
                        .backoffMultiplier(2.0)
                        .build())
                .build();
    }

    private static void demoEnvioSincrono(NotificationService service) {
        log.info("--- Demo 1: envio sincrono por Email ---");
        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@infinance-xp.pe")
                .subject("Bienvenido a InFinance XP")
                .message("Tu cuenta fue creada exitosamente.")
                .build();

        NotificationResult result = service.send(email);
        log.info("Resultado: {}", result);
    }

    private static void demoValidacionFallida(NotificationService service) {
        log.info("--- Demo 2: manejo de errores de validacion (try/catch) ---");
        SmsNotification smsInvalido = SmsNotification.builder()
                .recipient("987654321") // falta el "+" y el codigo de pais
                .message("Tu codigo es 1234")
                .build();

        try {
            service.send(smsInvalido);
        } catch (ValidationException e) {
            log.warn("Validacion fallo como se esperaba: {}", e.getMessage());
        }
    }

    private static void demoTemplates(NotificationService service) {
        log.info("--- Demo 3: notificacion Push usando un template ---");
        TemplateRegistry templates = new TemplateRegistry();
        templates.register(new NotificationTemplate("push.pago-recibido",
                "Hola {{nombre}}, recibimos tu pago de S/ {{monto}} correctamente."));

        TemplateEngine engine = new TemplateEngine();
        String cuerpo = templates.find("push.pago-recibido")
                .map(t -> engine.render(t, Map.of("nombre", "Ana", "monto", "150.00")))
                .orElseThrow();

        PushNotification push = PushNotification.builder()
                .recipient("fake-device-token-abc123")
                .title("Pago recibido")
                .message(cuerpo)
                .build();

        NotificationResult result = service.send(push);
        log.info("Resultado: {}", result);
    }

    private static void demoAsincronoYLote(NotificationService service) {
        log.info("--- Demo 4: envio asincrono (con reintentos) y envio en lote ---");

        SmsNotification smsConReintentos = SmsNotification.builder()
                .recipient("+51987654321")
                .message("Tu codigo de verificacion es 4821")
                .build();

        CompletableFuture<NotificationResult> future = service.sendAsync(smsConReintentos);
        future.thenAccept(result -> log.info("SMS async resuelto tras {} intento(s): {}",
                result.getAttempts(), result));

        SlackNotification alerta = SlackNotification.builder()
                .recipient("#alertas-produccion")
                .message("Nuevo despliegue completado en produccion.")
                .username("deploy-bot")
                .build();

        List<NotificationResult> lote = service.sendBatch(List.of(smsConReintentos, alerta));
        log.info("Resultados del lote: {}", lote);
    }
}
