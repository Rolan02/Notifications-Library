# 📬 Notification Library

Una librería Java agnóstica a frameworks para envío unificado de notificaciones a través de múltiples canales (Email, SMS, Push Notifications).

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📑 Tabla de Contenidos

- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Quick Start](#quick-start)
- [Configuración](#configuración)
    - [Email (SendGrid)](#email-sendgrid)
    - [SMS (Twilio)](#sms-twilio)
    - [Push (Firebase)](#push-firebase)
- [Uso Detallado](#uso-detallado)
    - [Email](#envío-de-email)
    - [SMS](#envío-de-sms)
    - [Push Notifications](#envío-de-push-notifications)
    - [NotificationService (Facade)](#notificationservice-facade-unificado)
    - [Retry Policies](#retry-policies)
    - [Event Publisher](#event-publisher-pubsub)
- [Proveedores Soportados](#proveedores-soportados)
- [API Reference](#api-reference)
- [Seguridad](#seguridad)
- [Arquitectura](#arquitectura)
- [Extensibilidad](#extensibilidad)
- [Testing](#testing)
- [Docker](#docker)
- [Contribución](#contribución)
- [Uso de IA](#uso-de-ia)

---

## ✨ Características

- **🔌 Agnóstico a Frameworks**: No depende de Spring, Quarkus, ni ningún framework específico
- **📧 Email**: Soporte para SendGrid, Mailgun (simulados)
- **📱 SMS**: Soporte para Twilio (simulado)
- **🔔 Push Notifications**: Soporte para Firebase Cloud Messaging (simulado)
- **🎯 API Unificada**: NotificationService para gestionar todos los canales desde un solo punto
- **🔄 Retry Policies**: Reintentos configurables con múltiples estrategias (Exponential, Linear, Fixed)
- **📡 Event-Driven**: Sistema Pub/Sub para tracking de eventos del lifecycle
- **⚡ Async Support**: Envío asíncrono con CompletableFuture
- **📦 Batch Operations**: Envío en lote de notificaciones
- **✅ Validación Robusta**: Validadores específicos por canal (email format, E.164 phone, device tokens)
- **🎨 SOLID Principles**: Arquitectura extensible y mantenible
- **🧪 Testing**: >80% coverage con 93+ tests unitarios

---

## 📋 Requisitos

- **Java**: 21 o superior
- **Maven**: 3.9+
- **Memoria**: Mínimo 256MB RAM

### Dependencias Principales

```xml
<!-- Lombok (boilerplate reduction) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
</dependency>

<!-- SLF4J (logging) -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>

<!-- Jackson (JSON) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>

<!-- Apache Commons Validator (email/phone validation) -->
<dependency>
    <groupId>commons-validator</groupId>
    <artifactId>commons-validator</artifactId>
    <version>1.8.0</version>
</dependency>
```

---

## 🚀 Instalación

### Maven

Agrega la dependencia a tu `pom.xml`:

```xml
<dependency>
    <groupId>com.company</groupId>
    <artifactId>notification-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Desde el código fuente

```bash
# Clonar el repositorio
git clone https://github.com/your-org/notification-lib.git
cd notification-lib

# Compilar e instalar
mvn clean install

# Ejecutar tests
mvn test
```

---

## ⚡ Quick Start

### Ejemplo Básico - Email

```java
// 1. Configurar el provider
EmailProviderConfig config = EmailProviderConfig.builder()
    .apiKey("SG.your_sendgrid_api_key")
    .fromEmail("noreply@yourcompany.com")
    .fromName("Your Company")
    .build();

// 2. Crear provider y sender
EmailProvider provider = new MockSendGridProvider(config);
NotificationSender<EmailNotification> sender = new EmailNotificationSender(provider);

// 3. Crear notificación
EmailNotification email = EmailNotification.builder()
    .recipient("user@example.com")
    .subject("Welcome to Our Service!")
    .content(NotificationContent.builder()
        .body("Thank you for signing up!")
        .build())
    .build();

// 4. Enviar
NotificationResult result = sender.send(email);

// 5. Verificar resultado
if (result.isSuccess()) {
    NotificationResult.Success success = (NotificationResult.Success) result;
    System.out.println("✅ Email sent! ID: " + success.providerMessageId());
} else {
    NotificationResult.Failure failure = (NotificationResult.Failure) result;
    System.out.println("❌ Failed: " + failure.errorMessage());
}
```

---

## ⚙️ Configuración

### Email (SendGrid)

```java
EmailProviderConfig emailConfig = EmailProviderConfig.builder()
    .apiKey("SG.your_api_key_here")              // Requerido
    .fromEmail("noreply@yourcompany.com")        // Requerido
    .fromName("Your Company")                    // Opcional
    .replyToEmail("support@yourcompany.com")     // Opcional
    .trackOpens(true)                            // Opcional (default: false)
    .trackClicks(true)                           // Opcional (default: false)
    .sandboxMode(false)                          // Opcional (default: false)
    .build();

MockSendGridProvider provider = new MockSendGridProvider(emailConfig);
```

**Configuración Alternativa - Mailgun:**

```java
EmailProviderConfig mailgunConfig = EmailProviderConfig.builder()
    .apiKey("key-your_mailgun_api_key")
    .apiBaseUrl("https://api.mailgun.net/v3/")
    .fromEmail("noreply@yourdomain.com")
    .build();

MockMailgunProvider provider = new MockMailgunProvider(mailgunConfig);
```

---

### SMS (Twilio)

```java
SmsProviderConfig smsConfig = SmsProviderConfig.builder()
    .accountSid("ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")  // Requerido
    .authToken("your_auth_token")                      // Requerido
    .fromPhoneNumber("+15551234567")                   // Requerido (E.164 format)
    .requestDeliveryStatus(true)                       // Opcional
    .statusCallbackUrl("https://yourapp.com/webhook")  // Opcional
    .maxPricePerSms(0.15)                              // Opcional (previene cargos altos)
    .validityPeriod(86400)                             // Opcional (24 horas default)
    .build();

MockTwilioProvider provider = new MockTwilioProvider(smsConfig);
```

**Uso de Short Code:**

```java
SmsProviderConfig shortCodeConfig = SmsProviderConfig.builder()
    .accountSid("ACxxxxxxxx")
    .authToken("your_token")
    .shortCode("12345")  // En lugar de fromPhoneNumber
    .build();
```

---

### Push (Firebase)

```java
PushProviderConfig pushConfig = PushProviderConfig.builder()
    .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")  // Requerido
    .projectId("your-firebase-project")                      // Requerido
    .enableAnalytics(true)                                   // Opcional
    .defaultTtl(86400)                                       // Opcional (24h default)
    .dryRun(false)                                           // Opcional (test mode)
    .useSandbox(false)                                       // Opcional
    .build();

MockFirebaseProvider provider = new MockFirebaseProvider(pushConfig);
```

**Autenticación con Service Account:**

```java
PushProviderConfig authConfig = PushProviderConfig.builder()
    .serverKey("AAAA...")
    .serviceAccountJson("/path/to/service-account.json")
    .projectId("your-project")
    .build();
```

---

## 📖 Uso Detallado

### Envío de Email

#### Email Básico

```java
EmailNotification email = EmailNotification.builder()
    .recipient("user@example.com")
    .subject("Your Subject Here")
    .content(NotificationContent.builder()
        .body("Email body text")
        .build())
    .build();

NotificationResult result = sender.send(email);
```

#### Email con CC/BCC

```java
EmailNotification email = EmailNotification.builder()
    .recipient("customer@example.com")
    .subject("Order Confirmation")
    .content(NotificationContent.builder()
        .body("Your order has been confirmed.")
        .build())
    .build();

// Agregar destinatarios CC y BCC
email.addCc("manager@yourcompany.com");
email.addCc("sales@yourcompany.com");
email.addBcc("archive@yourcompany.com");

sender.send(email);
```

#### Email HTML

```java
String htmlContent = """
    <html>
    <body style="font-family: Arial, sans-serif;">
        <h1>Welcome!</h1>
        <p>Thank you for joining our service.</p>
        <a href="https://yoursite.com">Get Started</a>
    </body>
    </html>
    """;

EmailNotification htmlEmail = EmailNotification.builder()
    .recipient("user@example.com")
    .subject("Welcome!")
    .htmlBody(htmlContent)
    .plainTextBody("Welcome! Thank you for joining.")
    .content(NotificationContent.builder()
        .body("Welcome! Thank you for joining.")
        .build())
    .build();
```

#### Email con Attachments

```java
EmailNotification email = EmailNotification.builder()
    .recipient("user@example.com")
    .subject("Invoice #12345")
    .content(NotificationContent.builder()
        .body("Please find your invoice attached.")
        .build())
    .build();

email.addAttachment("path/to/invoice.pdf");
email.addAttachment("path/to/receipt.pdf");

sender.send(email);
```

---

### Envío de SMS

#### SMS Básico

```java
SmsNotification sms = SmsNotification.builder()
    .recipient("+15559876543")  // E.164 format
    .content(NotificationContent.builder()
        .body("Your verification code is 123456")
        .build())
    .transactional(true)
    .build();

NotificationResult result = sender.send(sms);
```

#### SMS Internacional

```java
SmsNotification internationalSms = SmsNotification.builder()
    .recipient("+447700900123")  // UK number
    .content(NotificationContent.builder()
        .body("Your order has been shipped!")
        .build())
    .maxPrice(0.15)  // Prevenir cargos excesivos
    .build();

sender.send(internationalSms);
```

#### SMS con Status Callback

```java
SmsNotification smsWithCallback = SmsNotification.builder()
    .recipient("+15559876543")
    .content(NotificationContent.builder()
        .body("Package delivered!")
        .build())
    .statusCallbackUrl("https://yourapp.com/webhooks/sms-status")
    .validityPeriod(3600)  // 1 hora
    .build();
```

---

### Envío de Push Notifications

#### Push Básico

```java
PushNotification push = PushNotification.builder()
    .recipient("device_token_here")
    .title("New Message")
    .content(NotificationContent.builder()
        .body("You have a new message from Alice")
        .build())
    .platform(PushPlatform.ALL)
    .build();

NotificationResult result = sender.send(push);
```

#### Push con Imagen

```java
PushNotification richPush = PushNotification.builder()
    .recipient("device_token")
    .title("Flash Sale! 🔥")
    .content(NotificationContent.builder()
        .body("50% off on all electronics")
        .build())
    .imageUrl("https://yoursite.com/images/sale-banner.jpg")
    .clickAction("https://yoursite.com/flash-sale")
    .sound("default")
    .badge(1)
    .build();
```

#### Push con Data Payload

```java
PushNotification dataPayloadPush = PushNotification.builder()
    .recipient("device_token")
    .title("Order Update")
    .content(NotificationContent.builder()
        .body("Your order #12345 has been shipped!")
        .build())
    .build();

// Agregar datos custom
dataPayloadPush.addData("order_id", "12345");
dataPayloadPush.addData("tracking_number", "1Z999AA10123456784");
dataPayloadPush.addData("status", "shipped");
dataPayloadPush.addData("action", "view_order");

sender.send(dataPayloadPush);
```

#### Push de Alta Prioridad

```java
PushNotification urgentPush = PushNotification.builder()
    .recipient("device_token")
    .title("🚨 Security Alert")
    .content(NotificationContent.builder()
        .body("New login from unknown device in New York")
        .build())
    .priority(PushNotificationPriority.HIGH)
    .sound("alert")
    .ttl(300)  // 5 minutos (urgente)
    .build();
```

---

### NotificationService (Facade Unificado)

El **NotificationService** proporciona una API unificada para todos los canales:

```java
// 1. Configurar todos los providers
EmailProviderConfig emailConfig = EmailProviderConfig.builder()
    .apiKey("SG.key")
    .fromEmail("noreply@company.com")
    .build();

SmsProviderConfig smsConfig = SmsProviderConfig.builder()
    .accountSid("AC123")
    .authToken("token")
    .fromPhoneNumber("+15551234567")
    .build();

PushProviderConfig pushConfig = PushProviderConfig.builder()
    .serverKey("AAAA123")
    .projectId("project")
    .build();

// 2. Crear el servicio unificado
NotificationService service = NotificationService.builder()
    .withEmailProvider(new MockSendGridProvider(emailConfig))
    .withSmsProvider(new MockTwilioProvider(smsConfig))
    .withPushProvider(new MockFirebaseProvider(pushConfig))
    .build();

// 3. Enviar cualquier tipo de notificación
EmailNotification email = EmailNotification.builder()
    .recipient("user@example.com")
    .subject("Welcome")
    .content(NotificationContent.builder().body("Hello!").build())
    .build();

// El servicio detecta automáticamente el canal
NotificationResult result = service.send(email);

// O usar métodos específicos
service.sendEmail(email);
service.sendSms(smsNotification);
service.sendPush(pushNotification);

// Verificar disponibilidad de canales
boolean emailAvailable = service.isChannelAvailable(NotificationChannel.EMAIL);
boolean smsAvailable = service.isChannelAvailable(NotificationChannel.SMS);
```

---

### Retry Policies

Sistema de reintentos con múltiples estrategias:

#### Exponential Backoff

```java
RetryPolicy retryPolicy = RetryPolicy.builder()
    .maxAttempts(3)
    .baseDelayMs(1000)
    .strategy(new ExponentialBackoffStrategy())
    .logRetries(true)
    .build();

// Ejecutar con retry
NotificationResult result = retryPolicy.execute(() -> 
    sender.send(notification)
);

// Delays: 1s, 2s, 4s
```

#### Linear Backoff

```java
RetryPolicy linearRetry = RetryPolicy.builder()
    .maxAttempts(3)
    .baseDelayMs(1000)
    .strategy(new LinearBackoffStrategy())
    .build();

// Delays: 1s, 2s, 3s
```

#### Fixed Backoff

```java
RetryPolicy fixedRetry = RetryPolicy.builder()
    .maxAttempts(3)
    .baseDelayMs(1000)
    .strategy(new FixedBackoffStrategy())
    .build();

// Delays: 1s, 1s, 1s
```

#### Retry con Condiciones Específicas

```java
RetryPolicy conditionalRetry = RetryPolicy.builder()
    .maxAttempts(3)
    .baseDelayMs(1000)
    .strategy(new ExponentialBackoffStrategy())
    .retryOn(RateLimitException.class, NetworkException.class)
    .retryCondition(throwable -> {
        // Custom logic
        return throwable.getMessage().contains("timeout");
    })
    .build();
```

---

### Event Publisher (Pub/Sub)

Sistema de eventos para tracking del lifecycle:

```java
// 1. Crear publisher
NotificationEventPublisher publisher = new NotificationEventPublisher();

// 2. Subscribir listeners
publisher.subscribe(event -> {
    System.out.println("Event: " + event.getEventType());
});

publisher.subscribe(event -> {
    if (event.getEventType() == NotificationEventType.NOTIFICATION_FAILED) {
        alertSystem.notify(event);
    }
});

publisher.subscribe(event -> {
    if (event.getEventType() == NotificationEventType.NOTIFICATION_SENT) {
        analytics.track(event);
    }
});

// 3. Publicar eventos
publisher.publish(NotificationEvent.builder()
    .eventType(NotificationEventType.NOTIFICATION_CREATED)
    .notificationId("notif-123")
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .build());

publisher.publish(NotificationEvent.builder()
    .eventType(NotificationEventType.NOTIFICATION_SENT)
    .notificationId("notif-123")
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .providerMessageId("sg_abc123")
    .build());
```

**Tipos de Eventos:**
- `NOTIFICATION_CREATED` - Notificación creada y validada
- `NOTIFICATION_SENDING` - Enviando al provider
- `NOTIFICATION_SENT` - Enviada exitosamente
- `NOTIFICATION_DELIVERED` - Entregada al destinatario
- `NOTIFICATION_FAILED` - Fallo en el envío
- `NOTIFICATION_RETRYING` - Reintentando
- `NOTIFICATION_QUEUED` - En cola (async)

---

### Envío Asíncrono

```java
// CompletableFuture nativo
CompletableFuture<NotificationResult> future = sender.sendAsync(notification);

future.thenAccept(result -> {
    if (result.isSuccess()) {
        System.out.println("Sent asynchronously!");
    }
}).exceptionally(ex -> {
    System.err.println("Error: " + ex.getMessage());
    return null;
});

// Esperar resultado
NotificationResult result = future.get(); // Blocking
```

### Envío en Batch

```java
List<EmailNotification> emails = Arrays.asList(email1, email2, email3);

// Sync batch
List<NotificationResult> results = sender.sendBatch(emails);

// Async batch
CompletableFuture<List<NotificationResult>> futureResults = 
    sender.sendBatchAsync(emails);

// Procesar resultados
long successCount = results.stream()
    .filter(NotificationResult::isSuccess)
    .count();
```

---

## 📡 Proveedores Soportados

### Email
- ✅ **SendGrid** (simulado) - `MockSendGridProvider`
- ✅ **Mailgun** (simulado) - `MockMailgunProvider`

### SMS
- ✅ **Twilio** (simulado) - `MockTwilioProvider`

### Push Notifications
- ✅ **Firebase Cloud Messaging** (simulado) - `MockFirebaseProvider`

**Nota:** Los providers actuales son simulaciones realistas. Para producción, implementa providers reales siguiendo la misma interface.

---

## 📚 API Reference

### Core Interfaces

#### NotificationSender

```java
public interface NotificationSender<T extends Notification> {
    NotificationResult send(T notification);
    CompletableFuture<NotificationResult> sendAsync(T notification);
    List<NotificationResult> sendBatch(List<T> notifications);
    CompletableFuture<List<NotificationResult>> sendBatchAsync(List<T> notifications);
    NotificationChannel getChannel();
    boolean isReady();
}
```

#### NotificationProvider

```java
public interface NotificationProvider<T extends Notification> {
    ProviderResult send(T notification);
    String getProviderName();
    String getProviderType();
    boolean isConfigured();
    boolean healthCheck();
}
```

### Result Types

#### NotificationResult (Sealed Interface)

```java
public sealed interface NotificationResult {
    record Success(
        String notificationId,
        String providerMessageId,
        NotificationChannel channel,
        String recipient,
        NotificationStatus status,
        Instant timestamp,
        String message
    ) implements NotificationResult {}
    
    record Failure(
        String notificationId,
        NotificationChannel channel,
        String recipient,
        String errorCode,
        String errorMessage,
        Throwable cause,
        NotificationStatus status,
        Instant timestamp,
        boolean retryable
    ) implements NotificationResult {}
}
```

### Notification Models

#### EmailNotification

```java
EmailNotification.builder()
    .recipient(String)           // Requerido
    .subject(String)             // Requerido
    .content(NotificationContent) // Requerido
    .fromEmail(String)           // Opcional
    .fromName(String)            // Opcional
    .cc(List<String>)            // Opcional
    .bcc(List<String>)           // Opcional
    .replyTo(String)             // Opcional
    .htmlBody(String)            // Opcional
    .plainTextBody(String)       // Opcional
    .attachments(List<String>)   // Opcional
    .build();
```

#### SmsNotification

```java
SmsNotification.builder()
    .recipient(String)           // Requerido (E.164 format)
    .content(NotificationContent) // Requerido
    .fromPhoneNumber(String)     // Opcional
    .transactional(boolean)      // Opcional (default: true)
    .statusCallbackUrl(String)   // Opcional
    .maxPrice(Double)            // Opcional
    .validityPeriod(Integer)     // Opcional
    .build();
```

#### PushNotification

```java
PushNotification.builder()
    .recipient(String)           // Requerido (device token)
    .title(String)               // Requerido
    .content(NotificationContent) // Requerido
    .platform(PushPlatform)      // Opcional (default: ALL)
    .priority(PushNotificationPriority) // Opcional
    .badge(Integer)              // Opcional
    .sound(String)               // Opcional
    .imageUrl(String)            // Opcional
    .clickAction(String)         // Opcional
    .ttl(Integer)                // Opcional
    .data(Map<String, String>)   // Opcional
    .build();
```

---

## 🔒 Seguridad

### Mejores Prácticas para Manejar Credenciales

#### ❌ NO hacer:

```java
// NO hardcodear API keys
EmailProviderConfig config = EmailProviderConfig.builder()
    .apiKey("SG.hardcoded_key_12345")  // ❌ MAL
    .build();
```

#### ✅ SÍ hacer:

```java
// Usar variables de entorno
String apiKey = System.getenv("SENDGRID_API_KEY");
EmailProviderConfig config = EmailProviderConfig.builder()
    .apiKey(apiKey)  // ✅ BIEN
    .build();

// O usar un secrets manager
String apiKey = secretsManager.getSecret("sendgrid-api-key");
```

### Configuración Segura

```properties
# .env file (NO commitear a Git)
SENDGRID_API_KEY=SG.your_actual_key
TWILIO_ACCOUNT_SID=ACxxxxxxxxx
TWILIO_AUTH_TOKEN=your_token
FIREBASE_SERVER_KEY=AAAA123456
```

```java
// Cargar desde .env
String sendGridKey = System.getenv("SENDGRID_API_KEY");
String twilioSid = System.getenv("TWILIO_ACCOUNT_SID");
String twilioToken = System.getenv("TWILIO_AUTH_TOKEN");
```

### Logging Seguro

```java
// Las configs ya enmascaran datos sensibles automáticamente
logger.info("API Key: {}", config.getApiKeyMasked()); 
// Output: "API Key: SG.1234...6789"
```

### Recomendaciones

- ✅ Usa variables de entorno
- ✅ Usa secrets managers (AWS Secrets Manager, HashiCorp Vault)
- ✅ Nunca commitees credenciales a Git
- ✅ Rota las API keys regularmente
- ✅ Usa diferentes credenciales para dev/staging/production
- ✅ Habilita 2FA en las cuentas de proveedores

---

## 🏗️ Arquitectura

### Diagrama de Capas

```
┌─────────────────────────────────────────┐
│         NotificationService             │  ← Facade (API pública)
│      (Unified Entry Point)              │
└─────────────────────────────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
┌───────▼──────┐  ┌─▼──────┐  ┌▼─────────┐
│EmailSender   │  │SmsSender│  │PushSender│  ← Channel Layer
└───────┬──────┘  └─┬──────┘  └┬─────────┘
        │           │           │
        │     ┌─────┴─────┐     │
        │     │Validation │     │            ← Validation Layer
        │     └───────────┘     │
        │           │           │
┌───────▼──────┐  ┌─▼──────┐  ┌▼─────────┐
│EmailProvider │  │SmsProvider│ │PushProvider│ ← Provider Layer
└──────────────┘  └─────────┘  └──────────┘
        │           │           │
┌───────▼──────────▼───────────▼─────────┐
│   External APIs (SendGrid, Twilio...)  │  ← External Services
└────────────────────────────────────────┘
```

### Principios SOLID Aplicados

- **Single Responsibility**: Cada clase tiene una única responsabilidad
- **Open/Closed**: Extensible sin modificar código existente
- **Liskov Substitution**: Todos los providers son intercambiables
- **Interface Segregation**: Interfaces pequeñas y específicas
- **Dependency Inversion**: Dependencias en abstracciones, no implementaciones

### Design Patterns

1. **Facade**: NotificationService
2. **Strategy**: Providers, RetryStrategies
3. **Builder**: Configs, Models
4. **Observer**: Event Publisher/Listener
5. **Registry**: ChannelProviderRegistry
6. **Template Method**: AbstractValidator
7. **Result Type**: NotificationResult
8. **Factory**: (preparado para extensión)

---

## 🔧 Extensibilidad

### Agregar un Nuevo Canal (Slack)

#### 1. Crear el Modelo

```java
// SlackNotification.java
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SlackNotification extends Notification {
    private String channel;
    private String webhookUrl;
    
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }
}
```

#### 2. Crear el Provider

```java
// SlackProvider.java (interface)
public interface SlackProvider extends NotificationProvider<SlackNotification> {
    default boolean supportsThreads() {
        return true;
    }
}

// MockSlackProvider.java (implementación)
public class MockSlackProvider implements SlackProvider {
    @Override
    public ProviderResult send(SlackNotification notification) {
        // Implementación
    }
}
```

#### 3. Crear el Sender

```java
// SlackNotificationSender.java
public class SlackNotificationSender implements NotificationSender<SlackNotification> {
    private final SlackProvider provider;
    
    @Override
    public NotificationResult send(SlackNotification notification) {
        // Validación + envío + transformación
    }
}
```

#### 4. Actualizar NotificationChannel

```java
public enum NotificationChannel {
    EMAIL, SMS, PUSH, SLACK  // ← Agregar SLACK
}
```

**¡Sin modificar código existente!** ✅ Open/Closed Principle

---

### Agregar un Nuevo Provider (Mailchimp para Email)

```java
public class MailchimpProvider implements EmailProvider {
    private final EmailProviderConfig config;
    
    @Override
    public ProviderResult send(EmailNotification notification) {
        // Implementar integración con Mailchimp API
    }
    
    @Override
    public String getProviderName() {
        return "Mailchimp";
    }
}
```

Uso:

```java
NotificationService service = NotificationService.builder()
    .withEmailProvider(new MailchimpProvider(config))  // ← Nuevo provider
    .build();
```

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=EmailNotificationSenderTest

# Con coverage
mvn clean test jacoco:report
```

### Estructura de Tests

```
src/test/java/
├── core/
│   ├── model/NotificationResultTest.java
│   └── validation/
│       ├── EmailValidatorTest.java
│       ├── SmsValidatorTest.java
│       └── PushValidatorTest.java
├── channel/
│   ├── email/EmailNotificationSenderTest.java
│   ├── sms/SmsNotificationSenderTest.java
│   └── push/PushNotificationSenderTest.java
├── provider/
│   ├── email/MockSendGridProviderTest.java
│   ├── sms/MockTwilioProviderTest.java
│   └── push/MockFirebaseProviderTest.java
├── config/NotificationServiceTest.java
├── util/RetryPolicyTest.java
├── event/NotificationEventPublisherTest.java
└── MainTest.java
```

### Ejemplo de Test

```java
@Test
void shouldSendEmailSuccessfully() {
    // Given
    EmailProvider mockProvider = mock(EmailProvider.class);
    when(mockProvider.send(any())).thenReturn(
        ProviderResult.success("msg-123", "Sent")
    );
    
    EmailNotificationSender sender = new EmailNotificationSender(mockProvider);
    EmailNotification email = createValidEmail();
    
    // When
    NotificationResult result = sender.send(email);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
    verify(mockProvider).send(email);
}
```

### Coverage Actual

- **Total**: >80%
- **93 tests unitarios**
- **Todas las funcionalidades críticas cubiertas**

---

## 🐳 Docker

### Dockerfile

Consulta el archivo `Dockerfile` en la raíz del proyecto.

### Uso con Docker

```bash
# Build
docker build -t notification-lib .

# Run examples
docker run notification-lib

# Run specific example
docker run notification-lib java -cp target/notification-lib-1.0.0.jar \
    com.company.notifications.examples.EmailExample
```

### Docker Compose (Opcional)

```yaml
version: '3.8'
services:
  notification-lib:
    build: .
    environment:
      - SENDGRID_API_KEY=${SENDGRID_API_KEY}
      - TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
      - TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
```

---

## 🤝 Contribución

### Cómo Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Guías de Estilo

- **Java Code Style**: Google Java Style Guide
- **Commits**: Conventional Commits
- **Tests**: Coverage mínimo 80%

---

## 🤖 Uso de IA

Este proyecto fue desarrollado con asistencia de **Claude (Anthropic)** como herramienta de pair programming.

### Proceso de Trabajo con IA

**Modelo utilizado:** Claude 3.5 Sonnet (Anthropic)

**Estrategia de prompts:**
- Definición clara de arquitectura antes de codificar
- Iteración incremental por fases (FASE 1-8)
- Validación de principios SOLID en cada componente
- Revisión de patrones de diseño aplicados

**Decisiones tomadas por el desarrollador:**
- Estructura de paquetes y organización
- Elección de patrones de diseño (Strategy, Facade, Observer, etc.)
- Diseño de APIs públicas
- Estrategias de retry
- Sistema de eventos

**Decisiones sugeridas por la IA:**
- Sealed interfaces para NotificationResult (Java 21)
- Nombres específicos de clases y métodos
- Estructura de tests
- Documentación y ejemplos

**Áreas donde la IA ayudó:**
- Generación de código boilerplate
- Implementación de validadores
- Mock providers realistas
- Tests unitarios exhaustivos
- Documentación completa

**Áreas donde la IA no ayudó:**
- Toma de decisiones arquitectónicas principales
- Definición de requisitos y alcance
- Selección de tecnologías

---

## 👥 Autores

- **Rolando Mamani Salas** - *Desarrollo inicial* - [https://github.com/Rolan02](https://github.com/Rolan02)

