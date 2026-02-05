package com.company.notifications.examples;

import com.company.notifications.config.*;
import com.company.notifications.core.model.*;
import com.company.notifications.event.NotificationEvent;
import com.company.notifications.event.NotificationEventPublisher;
import com.company.notifications.event.NotificationEventType;
import com.company.notifications.provider.email.MockSendGridProvider;
import com.company.notifications.provider.push.MockFirebaseProvider;
import com.company.notifications.provider.sms.MockTwilioProvider;
import com.company.notifications.util.ExponentialBackoffStrategy;
import com.company.notifications.util.RetryPolicy;

/**
 * Comprehensive example demonstrating all advanced features:
 * - NotificationService (unified facade)
 * - RetryPolicy with strategies
 * - Event Publisher (Pub/Sub)
 * - Multi-channel usage
 */
public class ComprehensiveExample {

    public static void main(String[] args) {
        System.out.println("=== Comprehensive Notification Library Example ===\n");

        // Example 1: Unified NotificationService
        unifiedServiceExample();

        System.out.println("\n---\n");

        // Example 2: Retry Policy
        retryPolicyExample();

        System.out.println("\n---\n");

        // Example 3: Event Publisher
        eventPublisherExample();

        System.out.println("\n---\n");

        // Example 4: Complete workflow with all features
        completeWorkflowExample();
    }

    /**
     * Example 1: Using NotificationService for unified API
     */
    private static void unifiedServiceExample() {
        System.out.println("Example 1: Unified NotificationService");

        // Configure all providers
        EmailProviderConfig emailConfig = EmailProviderConfig.builder()
                .apiKey("SG.test_key")
                .fromEmail("noreply@company.com")
                .build();

        SmsProviderConfig smsConfig = SmsProviderConfig.builder()
                .accountSid("AC12345")
                .authToken("token")
                .fromPhoneNumber("+15551234567")
                .build();

        PushProviderConfig pushConfig = PushProviderConfig.builder()
                .serverKey("AAAA12345")
                .projectId("test-project")
                .build();

        // Build unified service
        NotificationService service = NotificationService.builder()
                .withEmailProvider(new MockSendGridProvider(emailConfig))
                .withSmsProvider(new MockTwilioProvider(smsConfig))
                .withPushProvider(new MockFirebaseProvider(pushConfig))
                .build();

        // Send different types of notifications using the same service

        // Send email
        EmailNotification email = EmailNotification.builder()
                .recipient("user@example.com")
                .subject("Welcome!")
                .content(NotificationContent.builder().body("Thanks for signing up!").build())
                .build();

        NotificationResult emailResult = service.sendEmail(email);
        System.out.println("Email: " + (emailResult.isSuccess() ? "✅ Sent" : "❌ Failed"));

        // Send SMS
        SmsNotification sms = SmsNotification.builder()
                .recipient("+15559876543")
                .content(NotificationContent.builder().body("Your code: 123456").build())
                .build();

        NotificationResult smsResult = service.sendSms(sms);
        System.out.println("SMS: " + (smsResult.isSuccess() ? "✅ Sent" : "❌ Failed"));

        // Send push
        PushNotification push = PushNotification.builder()
                .recipient("device_token_here")
                .title("New Message")
                .content(NotificationContent.builder().body("You have 1 new message").build())
                .build();

        NotificationResult pushResult = service.sendPush(push);
        System.out.println("Push: " + (pushResult.isSuccess() ? "✅ Sent" : "❌ Failed"));

        // Check channel availability
        System.out.println("\nChannel Status:");
        System.out.println("  Email: " + (service.isChannelAvailable(NotificationChannel.EMAIL) ? "✅" : "❌"));
        System.out.println("  SMS: " + (service.isChannelAvailable(NotificationChannel.SMS) ? "✅" : "❌"));
        System.out.println("  Push: " + (service.isChannelAvailable(NotificationChannel.PUSH) ? "✅" : "❌"));
    }

    /**
     * Example 2: Using RetryPolicy
     */
    private static void retryPolicyExample() {
        System.out.println("Example 2: Retry Policy with Exponential Backoff");

        // Create retry policy
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(100) // Short delay for demo
                .strategy(new ExponentialBackoffStrategy())
                .logRetries(true)
                .build();

        System.out.println("Strategy: " + retryPolicy.getStrategy().getName());
        System.out.println("Max attempts: " + retryPolicy.getMaxAttempts());
        System.out.println("\nAttempting operation that fails twice then succeeds...\n");

        // Simulate operation that fails twice then succeeds
        final int[] attemptCount = {0};

        try {
            String result = retryPolicy.execute(() -> {
                attemptCount[0]++;
                System.out.println("  > Attempt " + attemptCount[0]);

                if (attemptCount[0] < 3) {
                    throw new RuntimeException("Transient failure");
                }

                return "Success!";
            });

            System.out.println("\n✅ Operation succeeded: " + result);
            System.out.println("Total attempts: " + attemptCount[0]);

        } catch (Exception e) {
            System.out.println("❌ Operation failed after retries: " + e.getMessage());
        }
    }

    /**
     * Example 3: Using Event Publisher
     */
    private static void eventPublisherExample() {
        System.out.println("Example 3: Event Publisher (Pub/Sub)");

        // Create event publisher
        NotificationEventPublisher publisher = new NotificationEventPublisher();

        // Subscribe listeners
        publisher.subscribe(event -> {
            System.out.println("  [Listener 1] " + event.getEventType() +
                    " - Notification: " + event.getNotificationId());
        });

        publisher.subscribe(event -> {
            if (event.getEventType() == NotificationEventType.NOTIFICATION_FAILED) {
                System.out.println("  [Alert System] ⚠️  Notification failed: " +
                        event.getErrorCode());
            }
        });

        publisher.subscribe(event -> {
            if (event.getEventType() == NotificationEventType.NOTIFICATION_SENT) {
                System.out.println("  [Analytics] 📊 Tracking notification sent to " +
                        event.getChannel());
            }
        });

        System.out.println("Registered " + publisher.getListenerCount() + " listeners\n");

        // Publish events
        NotificationEvent event1 = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_CREATED)
                .notificationId("notif-001")
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .build();

        publisher.publish(event1);

        NotificationEvent event2 = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_SENT)
                .notificationId("notif-001")
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .providerMessageId("sg_abc123")
                .build();

        publisher.publish(event2);

        NotificationEvent event3 = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_FAILED)
                .notificationId("notif-002")
                .channel(NotificationChannel.SMS)
                .recipient("+15559876543")
                .errorCode("RATE_LIMIT")
                .errorMessage("Rate limit exceeded")
                .build();

        publisher.publish(event3);
    }

    /**
     * Example 4: Complete workflow with all features combined
     */
    private static void completeWorkflowExample() {
        System.out.println("Example 4: Complete Workflow (Service + Retry + Events)");

        // 1. Setup event publisher
        NotificationEventPublisher eventPublisher = new NotificationEventPublisher();

        eventPublisher.subscribe(event -> {
            System.out.println("  [Event] " + event.getEventType() +
                    " for " + event.getChannel());
        });

        // 2. Setup retry policy
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(2)
                .baseDelayMs(50)
                .strategy(new ExponentialBackoffStrategy())
                .logRetries(false) // Silent for cleaner output
                .build();

        // 3. Configure service
        EmailProviderConfig emailConfig = EmailProviderConfig.builder()
                .apiKey("SG.test_key")
                .fromEmail("app@company.com")
                .build();

        NotificationService service = NotificationService.builder()
                .withEmailProvider(new MockSendGridProvider(emailConfig))
                .build();

        // 4. Send notification with all features
        System.out.println("\nSending notification with retry and events...\n");

        EmailNotification email = EmailNotification.builder()
                .recipient("customer@example.com")
                .subject("Order Confirmation")
                .content(NotificationContent.builder()
                        .body("Your order #12345 has been confirmed!")
                        .build())
                .build();

        // Publish creation event
        eventPublisher.publish(NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_CREATED)
                .notificationId(email.getMetadata().getNotificationId())
                .channel(email.getChannel())
                .recipient(email.getRecipient())
                .build());

        try {
            // Execute with retry
            NotificationResult result = retryPolicy.execute(() -> {
                eventPublisher.publish(NotificationEvent.builder()
                        .eventType(NotificationEventType.NOTIFICATION_SENDING)
                        .notificationId(email.getMetadata().getNotificationId())
                        .channel(email.getChannel())
                        .recipient(email.getRecipient())
                        .build());

                return service.sendEmail(email);
            });

            if (result.isSuccess()) {
                NotificationResult.Success success = (NotificationResult.Success) result;

                eventPublisher.publish(NotificationEvent.builder()
                        .eventType(NotificationEventType.NOTIFICATION_SENT)
                        .notificationId(success.notificationId())
                        .channel(success.channel())
                        .recipient(success.recipient())
                        .providerMessageId(success.providerMessageId())
                        .message(success.message())
                        .build());

                System.out.println("\n✅ Notification sent successfully!");
                System.out.println("   ID: " + success.notificationId());
                System.out.println("   Provider ID: " + success.providerMessageId());
            }

        } catch (Exception e) {
            eventPublisher.publish(NotificationEvent.builder()
                    .eventType(NotificationEventType.NOTIFICATION_FAILED)
                    .notificationId(email.getMetadata().getNotificationId())
                    .channel(email.getChannel())
                    .recipient(email.getRecipient())
                    .errorMessage(e.getMessage())
                    .build());

            System.out.println("❌ Notification failed: " + e.getMessage());
        }

        System.out.println("\nWorkflow complete!");
    }
}
