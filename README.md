# notifications-lib

Librería Java **agnóstica a framework** para el envío unificado de notificaciones
multi-canal (Email, SMS, Push y Slack), con abstracción total del proveedor
concreto (SendGrid/Mailgun, Twilio, FCM, Slack Webhooks).

> Challenge técnico backend — proceso de selección **NOVACOMP** / cliente **InFinance XP** (Perú).

No requiere Spring, Quarkus ni ningún otro framework: toda la configuración se
hace con clases Java puras (Builders), por lo que puede integrarse en
cualquier tipo de aplicación Java (batch, monolito, microservicio, CLI, etc.).

---

## Tabla de contenidos

- [Instalación](#instalación)
- [Quick Start](#quick-start)
- [Conceptos clave](#conceptos-clave)
- [Configuración por canal / proveedor](#configuración-por-canal--proveedor)
- [Manejo de errores](#manejo-de-errores)
- [Reintentos (retry)](#reintentos-retry)
- [Envío asíncrono y en lote](#envío-asíncrono-y-en-lote)
- [Templates de mensajes](#templates-de-mensajes)
- [Estado de la notificación (Pub/Sub)](#estado-de-la-notificación-pubsub)
- [Cómo agregar un nuevo canal o proveedor](#cómo-agregar-un-nuevo-canal-o-proveedor)
- [Proveedores soportados](#proveedores-soportados)
- [API Reference](#api-reference)
- [Seguridad](#seguridad)
- [Arquitectura y principios de diseño](#arquitectura-y-principios-de-diseño)
- [Testing](#testing)
- [Docker](#docker)
- [Qué falta / decisiones de alcance](#qué-falta--decisiones-de-alcance)
- [Uso de IA en este proyecto](#uso-de-ia-en-este-proyecto)

---

## Instalación

Requisitos: **Java 21+** y **Maven 3.9+**.

### Como dependencia local (Maven)

```bash
git clone <url-del-repo>
cd notifications-lib
mvn clean install
```

Y en el `pom.xml` del proyecto consumidor:

```xml
<dependency>
    <groupId>com.novacomp</groupId>
    <artifactId>notifications-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.novacomp:notifications-lib:1.0.0'
```

### Compilar y correr los tests

```bash
mvn clean test
```

### Generar el jar ejecutable (con dependencias incluidas)

```bash
mvn clean package
java -jar target/notifications-lib-1.0.0.jar
```

Esto corre `NotificationExamples`, la clase de demo incluida en la librería.

---

## Quick Start

```java
import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.config.SendGridConfig;
import com.novacomp.notifications.core.NotificationResult;
import com.novacomp.notifications.provider.email.SendGridEmailProvider;
import com.novacomp.notifications.service.NotificationService;
import com.novacomp.notifications.service.NotificationServiceBuilder;

public class Demo {
    public static void main(String[] args) {
        SendGridConfig config = SendGridConfig.builder()
                .apiKey(System.getenv("SENDGRID_API_KEY"))
                .fromEmail("no-reply@miempresa.com")
                .fromName("Mi Empresa")
                .build();

        NotificationService notifications = NotificationServiceBuilder.create()
                .registerEmailSender(new SendGridEmailProvider(config))
                .build();

        EmailNotification email = EmailNotification.builder()
                .recipient("cliente@dominio.com")
                .subject("Bienvenido")
                .message("Tu cuenta fue creada exitosamente.")
                .build();

        NotificationResult result = notifications.send(email);

        if (result.isSuccess()) {
            System.out.println("Enviado! id=" + result.getProviderMessageId());
        } else {
            System.out.println("Fallo: " + result.getErrorMessage());
        }
    }
}
```

La misma llamada `notifications.send(...)` funciona sin cambios para SMS, Push
o Slack — solo cambia el tipo de `Notification` que se construye y qué sender
se registró en el `NotificationServiceBuilder`.

---

## Conceptos clave

| Concepto | Rol |
|---|---|
| `Notification` (y subtipos `EmailNotification`, `SmsNotification`, `PushNotification`, `SlackNotification`) | Modelo inmutable de una notificación, construido con Builder. Cada canal agrega sus campos propios (`subject` en Email, `title` en Push, etc.) sin ensuciar el resto. |
| `NotificationSender<T>` | Contrato único (Strategy) que todo canal implementa: `send`, `sendAsync`. |
| `*Provider` (`EmailProvider`, `SmsProvider`, `PushProvider`, `SlackProvider`) | Puerto hacia el proveedor externo real (SendGrid, Twilio, FCM, Slack...). Es lo que cambia si migras de proveedor. |
| `NotificationService` | Facade: punto de entrada único para el consumidor de la librería. |
| `NotificationServiceBuilder` | Builder para ensamblar el `NotificationService` eligiendo proveedores, validadores y políticas de reintento. |
| `NotificationResult` | Resultado uniforme de un envío (éxito/fallo, id del proveedor, intentos). |

---

## Configuración por canal / proveedor

Toda la configuración es código Java puro (sin YAML/properties). Se recomienda
leer las credenciales desde variables de entorno o un vault, nunca hardcodeadas
(ver sección [Seguridad](#seguridad)).

### Email — SendGrid

```java
SendGridConfig config = SendGridConfig.builder()
        .apiKey(System.getenv("SENDGRID_API_KEY"))
        .fromEmail("no-reply@miempresa.com")
        .fromName("Mi Empresa")
        .build();

builder.registerEmailSender(new SendGridEmailProvider(config));
```

### Email — Mailgun (proveedor alternativo, mismo canal)

```java
MailgunConfig config = MailgunConfig.builder()
        .apiKey(System.getenv("MAILGUN_API_KEY"))
        .domain("mg.miempresa.com")
        .fromEmail("no-reply@miempresa.com")
        .build();

builder.registerEmailSender(new MailgunEmailProvider(config));
```

Cambiar de SendGrid a Mailgun es **una sola línea** en el punto de ensamblado;
el resto de la aplicación (que solo conoce `NotificationService`) no cambia.

### SMS — Twilio

```java
TwilioConfig config = TwilioConfig.builder()
        .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
        .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
        .fromNumber("+15005550006")
        .build();

builder.registerSmsSender(new TwilioSmsProvider(config));
```

### Push — Firebase Cloud Messaging

```java
FcmConfig config = FcmConfig.builder()
        .projectId("mi-proyecto-firebase")
        .serverKey(System.getenv("FCM_SERVER_KEY"))
        .build();

builder.registerPushSender(new FcmPushProvider(config));
```

### Slack (opcional) — Incoming Webhook

```java
SlackConfig config = SlackConfig.builder()
        .webhookUrl(System.getenv("SLACK_WEBHOOK_URL"))
        .build();

builder.registerSlackSender(new SlackWebhookProvider(config));
```

---

## Manejo de errores

La librería distingue explícitamente dos tipos de fallo:

1. **Errores de validación** (`ValidationException`, unchecked): datos de
   entrada inválidos — email mal formado, teléfono sin formato E.164, campos
   obligatorios vacíos. Se lanzan **antes** de intentar el envío y se
   propagan al llamador (es un error de uso, no de infraestructura).
2. **Errores de envío** (fallas del proveedor simulado): **no** se lanzan
   como excepción. Se reflejan en el `NotificationResult` devuelto con
   `status = FAILED` y `errorMessage` con el detalle. Esto permite enviar en
   lote sin necesitar un `try/catch` por cada notificación.

```java
try {
    NotificationResult result = notifications.send(sms);
    if (!result.isSuccess()) {
        log.warn("No se pudo enviar el SMS: {}", result.getErrorMessage());
    }
} catch (ValidationException e) {
    log.warn("Datos de la notificación inválidos: {}", e.getMessage());
}
```

---

## Reintentos (retry)

Implementado como **Decorator** (`RetryableNotificationSender`), envuelve
cualquier sender existente sin modificarlo:

```java
NotificationService notifications = NotificationServiceBuilder.create()
        .registerSmsSender(new TwilioSmsProvider(twilioConfig))
        .withRetry(NotificationChannel.SMS, RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelayMillis(200)
                .backoffMultiplier(2.0)
                .build())
        .build();
```

Solo se reintentan fallos de **envío**; un `ValidationException` se propaga en
el primer intento (reintentar datos inválidos no cambia el resultado).

---

## Envío asíncrono y en lote

```java
CompletableFuture<NotificationResult> future = notifications.sendAsync(email);
future.thenAccept(result -> log.info("Resultado: {}", result));

List<NotificationResult> resultados = notifications.sendBatch(List.of(email, sms, push));
```

`sendBatch` acepta notificaciones de canales mixtos y las envía en paralelo
usando `CompletableFuture` internamente.

---

## Templates de mensajes

Motor de templates minimalista con placeholders `{{variable}}`:

```java
TemplateRegistry templates = new TemplateRegistry();
templates.register(new NotificationTemplate(
        "push.pago-recibido",
        "Hola {{nombre}}, recibimos tu pago de S/ {{monto}}."));

String cuerpo = templates.find("push.pago-recibido")
        .map(t -> new TemplateEngine().render(t, Map.of("nombre", "Ana", "monto", "150.00")))
        .orElseThrow();
```

---

## Estado de la notificación (Pub/Sub)

Cualquier parte de la aplicación puede suscribirse a cambios de estado sin
acoplarse a los senders concretos (patrón Observer):

```java
notifications.subscribe(event ->
        metrics.increment("notifications." + event.getChannel() + "." + event.getStatus()));
```

---

## Cómo agregar un nuevo canal o proveedor

**Un nuevo proveedor para un canal existente** (ej. Amazon SES para Email):

1. Implementa `EmailProvider` (o el `*Provider` del canal correspondiente).
2. Créale su clase de configuración (`SesConfig`) con Builder.
3. Regístralo: `builder.registerEmailSender(new SesEmailProvider(sesConfig))`.

Cero cambios en `NotificationService`, en los demás senders ni en el código
cliente. Esto es Open/Closed en la práctica.

**Un canal completamente nuevo** (ej. WhatsApp):

1. Crea `WhatsAppNotification extends Notification` con sus campos propios.
2. Crea el puerto `WhatsAppProvider` y su implementación concreta.
3. Crea `WhatsAppNotificationSender extends AbstractNotificationSender<WhatsAppNotification>`
   implementando solo `doSend(...)`.
4. (Opcional) Agrega `registerWhatsAppSender(...)` a `NotificationServiceBuilder`,
   o simplemente usa el método genérico `registerSender(...)` que ya existe.

No se modifica ninguna clase existente del core, sender, service o exception.

---

## Proveedores soportados

| Canal | Proveedores incluidos | Referencia de API revisada |
|---|---|---|
| Email | SendGrid, Mailgun | `POST /v3/mail/send`, `POST /v3/{domain}/messages` |
| SMS | Twilio | `POST /2010-04-01/Accounts/{sid}/Messages.json` |
| Push | Firebase Cloud Messaging | `POST /v1/projects/{id}/messages:send` |
| Slack (opcional) | Incoming Webhooks | `POST {webhookUrl}` |

> Nota: no se realizan llamadas HTTP reales (alcance del challenge). Cada
> `*Provider` documenta en su Javadoc el endpoint, formato de request y
> respuesta reales del proveedor que simula.

---

## API Reference

### `NotificationService`
- `NotificationResult send(Notification n)`
- `CompletableFuture<NotificationResult> sendAsync(Notification n)`
- `List<NotificationResult> sendBatch(List<? extends Notification> n)`
- `void subscribe(NotificationEventListener listener)`
- `boolean supports(NotificationChannel channel)`

### `NotificationServiceBuilder`
- `registerEmailSender(EmailProvider [, validator])`
- `registerSmsSender(SmsProvider [, validator])`
- `registerPushSender(PushProvider [, validator])`
- `registerSlackSender(SlackProvider [, validator])`
- `registerSender(NotificationSender<?>)` — genérico, para canales custom
- `withRetry(NotificationChannel, RetryPolicy)`
- `addEventListener(NotificationEventListener)`
- `build()`

### `Notification` (abstracta) y subtipos
- `EmailNotification.builder().recipient(...).subject(...).message(...).htmlBody(...).attachmentNames(...).build()`
- `SmsNotification.builder().recipient(...).message(...).senderId(...).build()`
- `PushNotification.builder().recipient(...).title(...).message(...).data(...).build()`
- `SlackNotification.builder().recipient(...).message(...).username(...).iconEmoji(...).build()`

### `NotificationResult`
- `isSuccess()`, `getStatus()`, `getProviderMessageId()`, `getErrorMessage()`, `getAttempts()`

---

## Seguridad

- **Nunca** hardcodees API keys/tokens en el código fuente. Todas las clases
  `*Config` reciben las credenciales por parámetro; en producción léelas de
  variables de entorno, AWS Secrets Manager, HashiCorp Vault, etc.
- Los ejemplos (`NotificationExamples`) usan credenciales **falsas**
  únicamente para fines de demostración.
- Evita loggear payloads completos de notificaciones en producción si
  contienen PII (números de teléfono, emails, tokens de dispositivo); los
  providers de ejemplo solo loggean identificadores, no el contenido sensible
  completo — ajusta el nivel de log según tu política de datos.
- Si agregas un canal para InFinance XP u otro cliente financiero, valida
  especialmente que no se logueen datos sensibles (montos, cuentas) en texto
  plano en los logs de aplicación.

---

## Arquitectura y principios de diseño

**Patrones aplicados:**
- **Strategy** — `NotificationSender<T>` / `*Provider`: cada canal y cada
  proveedor es una estrategia intercambiable.
- **Template Method** — `AbstractNotificationSender`: valida → envía →
  traduce respuesta → publica evento; las subclases solo implementan `doSend`.
- **Builder** — construcción de `Notification`, `NotificationResult` y todas
  las clases `*Config` (incluye un builder genérico auto-referenciado en
  `Notification.Builder<B>` para no duplicar campos comunes en cada subtipo).
- **Facade** — `NotificationService` como único punto de entrada.
- **Decorator** — `RetryableNotificationSender` agrega reintentos a
  cualquier sender sin heredar ni modificar su código.
- **Observer / Pub-Sub** — `NotificationEventPublisher` +
  `NotificationEventListener` para notificar el estado del envío.

**SOLID:**
- **SRP** — cada clase tiene una responsabilidad (validar, enviar, construir,
  publicar eventos, reintentar) separada.
- **OCP** — agregar canal o proveedor nuevo no requiere tocar código existente
  (ver sección anterior).
- **LSP** — cualquier `NotificationSender<T>` (con o sin retry) es
  intercambiable donde se espera la interfaz.
- **ISP** — `EmailProvider`, `SmsProvider`, etc. son interfaces pequeñas y
  específicas por canal, en vez de una interfaz gigante con métodos opcionales.
- **DIP** — `NotificationService` y los senders dependen de abstracciones
  (`*Provider`, `NotificationValidator`) inyectadas por constructor, nunca de
  clases concretas de SendGrid/Twilio/etc.

---

## Testing

```bash
mvn clean test
```

Incluye tests unitarios (JUnit 5 + Mockito) para:
- Validadores (`EmailValidatorTest`, `PhoneValidatorTest`)
- Senders con proveedor mockeado, incluyendo el camino de validación fallida
  y de fallo de proveedor (`EmailNotificationSenderTest`)
- El decorator de reintentos (`RetryableNotificationSenderTest`)
- El facade `NotificationService` (dispatch por canal, canal no registrado,
  pub/sub, envío en lote) (`NotificationServiceTest`)
- El motor de templates (`TemplateEngineTest`)

> `MainTest.java` (paquete raíz) es un test trivial que falla a propósito;
> se agregó porque el documento del challenge lo pedía explícitamente como
> instrucción literal, no forma parte de la cobertura real del proyecto
> (ver el comentario Javadoc en el propio archivo).

---

## Docker

```bash
docker build -t notifications-lib .
docker run --rm notifications-lib
```

Compila la librería y ejecuta `NotificationExamples` dentro del contenedor,
sin necesidad de tener Java/Maven instalados localmente.

---

## Qué falta / decisiones de alcance

No hay librería 100% terminada; estas son las decisiones de priorización y lo
que dejaría documentado como siguiente paso:

- **Implementado:** los 3 canales obligatorios (Email, SMS, Push) + Slack
  opcional, validación, manejo de errores diferenciado, reintentos con
  backoff, envío async y en lote, templates, pub/sub de estado, Dockerfile.
- **No implementado / próximos pasos:**
  - Persistencia de historial de notificaciones (hoy todo vive en memoria del
    proceso; en un escenario real agregaría un `NotificationRepository`
    como puerto, para no acoplar la librería a ninguna base de datos concreta).
  - Rate limiting por proveedor (protegerse de límites de SendGrid/Twilio).
  - Circuit breaker alrededor de `*Provider` para cortar llamadas cuando un
    proveedor está caído de forma sostenida (hoy solo hay retry, no circuit
    breaker; encajaría como otro Decorator, igual que `RetryableNotificationSender`).
  - Métricas estructuradas (Micrometer) en vez de solo logs + eventos.
  - Validación de adjuntos de Email (tamaño, tipo MIME).
  - Soporte real de HTML templates con Freemarker/Thymeleaf detrás de
    `TemplateEngine` (hoy es placeholder simple a propósito, para no
    imponer una dependencia pesada).

---

## Uso de IA en este proyecto

<!-- Texto agregado según lo solicitado en el documento del challenge: declarar el uso de IA. -->

Este proyecto fue desarrollado con la asistencia de **Claude** (Anthropic),
usado como agente de desarrollo dentro de este mismo flujo de trabajo (Claude
en modo agente con acceso a un entorno de archivos/bash, no solo autocompletado
de código).

- **Proceso de trabajo:** se partió del documento del challenge tal cual fue
  entregado; antes de generar código se pidió a la IA confirmar que entendía
  el alcance completo y se resolvieron ambigüedades explícitamente (incluyendo
  una instrucción atípica embebida en el documento sobre un test que debía
  fallar a propósito) antes de avanzar.
- **Decisiones tomadas por la persona candidata:** el paquete de arquitectura
  final (Strategy + Template Method + Builder + Facade + Decorator + Observer),
  qué funcionalidades opcionales priorizar (retry, validación, templates,
  pub/sub, Slack, Docker), la estrategia de manejo de errores (excepción para
  validación vs. `Result` para fallos de envío), y qué documentar como
  pendiente en vez de improvisar una implementación a medias.
- **En qué ayudó la IA:** generar rápidamente el boilerplate repetitivo entre
  canales (los 4 senders y las 4 clases `*Notification` siguen el mismo
  esqueleto), redactar la documentación Javadoc de referencia de cada API de
  proveedor simulada, y armar la batería inicial de tests unitarios.
- **En qué no reemplazó el criterio propio:** las decisiones de diseño (por
  qué Decorator para retry y no una simple lista de reintentos dentro del
  sender; por qué separar validación de errores de envío; qué dejar fuera de
  alcance) fueron evaluadas y decididas por la persona candidata, no
  aceptadas "a ciegas" de una primera sugerencia.
