package com.company.notifications.examples;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.email.EmailNotificationSender;
import com.company.notifications.config.EmailProviderConfig;
import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.provider.email.MockSendGridProvider;

/**
 * Example demonstrating how to send email notifications.
 *
 * This shows the complete flow:
 * 1. Configure provider
 * 2. Create sender
 * 3. Build notification
 * 4. Send and handle result
 */
public class EmailExample {

    public static void main(String[] args) {
        System.out.println("=== Email Notification Example ===\n");

        // Example 1: Basic email
        basicEmailExample();

        System.out.println("\n---\n");

        // Example 2: Email with CC/BCC
        emailWithCcBccExample();

        System.out.println("\n---\n");

        // Example 3: HTML email
        htmlEmailExample();

        System.out.println("\n---\n");

        // Example 4: Handling failures
        handleFailuresExample();
    }

    /**
     * Example 1: Sending a basic email
     */
    private static void basicEmailExample() {
        System.out.println("Example 1: Basic Email");

        // Step 1: Configure the provider
        EmailProviderConfig config = EmailProviderConfig.builder()
                .apiKey("SG.test_api_key_12345")
                .fromEmail("noreply@mycompany.com")
                .fromName("My Company")
                .sandboxMode(false) // Set to true to not actually send
                .trackOpens(true)
                .trackClicks(true)
                .build();

        // Step 2: Create provider and sender
        MockSendGridProvider provider = new MockSendGridProvider(config);
        NotificationSender<EmailNotification> sender = new EmailNotificationSender(provider);

        // Step 3: Build the notification
        EmailNotification email = EmailNotification.builder()
                .recipient("user@example.com")
                .subject("Welcome to Our Service!")
                .content(NotificationContent.builder()
                        .body("Thank you for signing up. We're excited to have you!")
                        .build())
                .build();

        // Step 4: Send and handle result
        NotificationResult result = sender.send(email);

        if (result.isSuccess()) {
            NotificationResult.Success success = (NotificationResult.Success) result;
            System.out.println("✅ Email sent successfully!");
            System.out.println("   Notification ID: " + success.notificationId());
            System.out.println("   Provider Message ID: " + success.providerMessageId());
            System.out.println("   Recipient: " + success.recipient());
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Email failed to send!");
            System.out.println("   Error Code: " + failure.errorCode());
            System.out.println("   Error Message: " + failure.errorMessage());
        }
    }

    /**
     * Example 2: Email with CC and BCC
     */
    private static void emailWithCcBccExample() {
        System.out.println("Example 2: Email with CC/BCC");

        EmailProviderConfig config = EmailProviderConfig.builder()
                .apiKey("SG.test_api_key_12345")
                .fromEmail("sales@mycompany.com")
                .fromName("Sales Team")
                .build();

        MockSendGridProvider provider = new MockSendGridProvider(config);
        NotificationSender<EmailNotification> sender = new EmailNotificationSender(provider);

        EmailNotification email = EmailNotification.builder()
                .recipient("customer@example.com")
                .subject("Your Order Confirmation")
                .content(NotificationContent.builder()
                        .body("Your order #12345 has been confirmed and will ship soon.")
                        .build())
                .replyTo("support@mycompany.com")
                .build();

        // Add CC recipients
        email.addCc("manager@mycompany.com");
        email.addCc("accounting@mycompany.com");

        // Add BCC recipient (hidden from others)
        email.addBcc("archive@mycompany.com");

        NotificationResult result = sender.send(email);

        if (result.isSuccess()) {
            System.out.println("✅ Email with CC/BCC sent successfully!");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 3: HTML email
     */
    private static void htmlEmailExample() {
        System.out.println("Example 3: HTML Email");

        EmailProviderConfig config = EmailProviderConfig.builder()
                .apiKey("SG.test_api_key_12345")
                .fromEmail("newsletter@mycompany.com")
                .fromName("My Company Newsletter")
                .build();

        MockSendGridProvider provider = new MockSendGridProvider(config);
        NotificationSender<EmailNotification> sender = new EmailNotificationSender(provider);

        String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h1>Welcome!</h1>
                <p>Thank you for subscribing to our newsletter.</p>
                <p><a href="https://example.com">Visit our website</a></p>
            </body>
            </html>
            """;

        EmailNotification email = EmailNotification.builder()
                .recipient("subscriber@example.com")
                .subject("Welcome to Our Newsletter!")
                .htmlBody(htmlBody)
                .plainTextBody("Welcome! Thank you for subscribing. Visit us at https://example.com")
                .content(NotificationContent.builder()
                        .body("Welcome! Thank you for subscribing.")
                        .build())
                .build();

        NotificationResult result = sender.send(email);

        if (result.isSuccess()) {
            System.out.println("✅ HTML email sent successfully!");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 4: Handling different failure scenarios
     */
    private static void handleFailuresExample() {
        System.out.println("Example 4: Handling Failures");

        EmailProviderConfig config = EmailProviderConfig.builder()
                .apiKey("SG.test_api_key_12345")
                .fromEmail("test@mycompany.com")
                .build();

        MockSendGridProvider provider = new MockSendGridProvider(config);
        NotificationSender<EmailNotification> sender = new EmailNotificationSender(provider);

        // Try sending multiple emails to demonstrate random failures
        System.out.println("Sending 10 test emails to demonstrate success/failure handling:\n");

        for (int i = 1; i <= 10; i++) {
            EmailNotification email = EmailNotification.builder()
                    .recipient("test" + i + "@example.com")
                    .subject("Test Email #" + i)
                    .content(NotificationContent.builder()
                            .body("This is test email number " + i)
                            .build())
                    .build();

            NotificationResult result = sender.send(email);

            if (result.isSuccess()) {
                NotificationResult.Success success = (NotificationResult.Success) result;
                System.out.println("  [" + i + "] ✅ Success - ID: " +
                        success.providerMessageId().substring(0, 15) + "...");
            } else {
                NotificationResult.Failure failure = (NotificationResult.Failure) result;
                String retryable = failure.retryable() ? "(retryable)" : "(permanent)";
                System.out.println("  [" + i + "] ❌ Failed " + retryable + " - " +
                        failure.errorCode());
            }
        }
    }
}
