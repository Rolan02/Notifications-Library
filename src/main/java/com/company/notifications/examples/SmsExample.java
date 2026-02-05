package com.company.notifications.examples;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.sms.SmsNotificationSender;
import com.company.notifications.config.SmsProviderConfig;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.provider.sms.MockTwilioProvider;

import java.util.List;

/**
 * Example demonstrating how to send SMS notifications.
 *
 * This shows the complete flow:
 * 1. Configure provider (Twilio)
 * 2. Create sender
 * 3. Build notification
 * 4. Send and handle result
 */
public class SmsExample {

    public static void main(String[] args) {
        System.out.println("=== SMS Notification Example ===\n");

        // Example 1: Basic SMS
        basicSmsExample();

        System.out.println("\n---\n");

        // Example 2: International SMS
        internationalSmsExample();

        System.out.println("\n---\n");

        // Example 3: Long SMS (multiple segments)
        longSmsExample();

        System.out.println("\n---\n");

        // Example 4: Transactional SMS with callback
        transactionalSmsWithCallbackExample();

        System.out.println("\n---\n");

        // Example 5: Batch SMS sending
        batchSmsExample();
    }

    /**
     * Example 1: Sending a basic SMS
     */
    private static void basicSmsExample() {
        System.out.println("Example 1: Basic SMS");

        // Step 1: Configure the provider
        SmsProviderConfig config = SmsProviderConfig.builder()
                .accountSid("TWILIO_ACCOUNT_SID")
                .authToken("your_auth_token_here")
                .fromPhoneNumber("+15551234567")
                .useTestCredentials(false)
                .build();

        // Step 2: Create provider and sender
        MockTwilioProvider provider = new MockTwilioProvider(config);
        NotificationSender<SmsNotification> sender = new SmsNotificationSender(provider);

        // Step 3: Build the notification
        SmsNotification sms = SmsNotification.builder()
                .recipient("+15559876543")
                .content(NotificationContent.builder()
                        .body("Your verification code is 123456. Valid for 10 minutes.")
                        .build())
                .transactional(true)
                .build();

        // Step 4: Send and handle result
        NotificationResult result = sender.send(sms);

        if (result.isSuccess()) {
            NotificationResult.Success success = (NotificationResult.Success) result;
            System.out.println("✅ SMS sent successfully!");
            System.out.println("   Notification ID: " + success.notificationId());
            System.out.println("   Provider Message ID: " + success.providerMessageId());
            System.out.println("   Recipient: " + success.recipient());
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ SMS failed to send!");
            System.out.println("   Error Code: " + failure.errorCode());
            System.out.println("   Error Message: " + failure.errorMessage());
            System.out.println("   Retryable: " + failure.retryable());
        }
    }

    /**
     * Example 2: International SMS
     */
    private static void internationalSmsExample() {
        System.out.println("Example 2: International SMS");

        SmsProviderConfig config = SmsProviderConfig.builder()
                .accountSid("TWILIO_ACCOUNT_SID")
                .authToken("your_auth_token_here")
                .fromPhoneNumber("+15551234567")
                .maxPricePerSms(0.15) // Prevent expensive international charges
                .build();

        MockTwilioProvider provider = new MockTwilioProvider(config);
        NotificationSender<SmsNotification> sender = new SmsNotificationSender(provider);

        // Send to UK number
        SmsNotification sms = SmsNotification.builder()
                .recipient("+447700900123") // UK number
                .content(NotificationContent.builder()
                        .body("Hello from the US! Your order has been shipped.")
                        .build())
                .maxPrice(0.15)
                .build();

        NotificationResult result = sender.send(sms);

        if (result.isSuccess()) {
            System.out.println("✅ International SMS sent to UK!");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 3: Long SMS (multiple segments)
     */
    private static void longSmsExample() {
        System.out.println("Example 3: Long SMS (Multiple Segments)");

        SmsProviderConfig config = SmsProviderConfig.builder()
                .accountSid("TWILIO_ACCOUNT_SID")
                .authToken("your_auth_token_here")
                .fromPhoneNumber("+15551234567")
                .build();

        MockTwilioProvider provider = new MockTwilioProvider(config);
        NotificationSender<SmsNotification> sender = new SmsNotificationSender(provider);

        // Long message that will be split into multiple segments
        String longMessage = """
            Dear customer,
            
            Your appointment has been confirmed for Monday, March 15th at 2:00 PM.
            Please arrive 10 minutes early to complete any necessary paperwork.
            
            Location: 123 Main St, Suite 400
            
            If you need to reschedule, please call us at (555) 123-4567 or reply CANCEL to this message.
            
            Thank you!
            """;

        SmsNotification sms = SmsNotification.builder()
                .recipient("+15559876543")
                .content(NotificationContent.builder()
                        .body(longMessage)
                        .build())
                .build();

        System.out.println("   Message length: " + longMessage.length() + " characters");
        System.out.println("   Estimated segments: " + (int) Math.ceil((double) longMessage.length() / 153));

        NotificationResult result = sender.send(sms);

        if (result.isSuccess()) {
            System.out.println("✅ Long SMS sent successfully (will arrive as multiple messages)");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 4: Transactional SMS with delivery status callback
     */
    private static void transactionalSmsWithCallbackExample() {
        System.out.println("Example 4: Transactional SMS with Status Callback");

        SmsProviderConfig config = SmsProviderConfig.builder()
                .accountSid("TWILIO_ACCOUNT_SID")
                .authToken("your_auth_token_here")
                .fromPhoneNumber("+15551234567")
                .requestDeliveryStatus(true)
                .statusCallbackUrl("https://myapp.com/webhooks/sms-status")
                .build();

        MockTwilioProvider provider = new MockTwilioProvider(config);
        NotificationSender<SmsNotification> sender = new SmsNotificationSender(provider);

        SmsNotification sms = SmsNotification.builder()
                .recipient("+15559876543")
                .content(NotificationContent.builder()
                        .body("Your package has been delivered! Confirmation #ABC123")
                        .build())
                .transactional(true)
                .statusCallbackUrl("https://myapp.com/webhooks/sms-status/order-123")
                .validityPeriod(3600) // 1 hour validity
                .build();

        NotificationResult result = sender.send(sms);

        if (result.isSuccess()) {
            System.out.println("✅ Transactional SMS sent!");
            System.out.println("   Status updates will be sent to the callback URL");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 5: Sending SMS in batch
     */
    private static void batchSmsExample() {
        System.out.println("Example 5: Batch SMS Sending");

        SmsProviderConfig config = SmsProviderConfig.builder()
                .accountSid("TWILIO_ACCOUNT_SID")
                .authToken("your_auth_token_here")
                .fromPhoneNumber("+15551234567")
                .build();

        MockTwilioProvider provider = new MockTwilioProvider(config);
        NotificationSender<SmsNotification> sender = new SmsNotificationSender(provider);

        // Create multiple SMS notifications
        var phoneNumbers = new String[]{
                "+15559876543",
                "+15559876544",
                "+15559876545",
                "+15559876546",
                "+15559876547"
        };

        var notifications = java.util.Arrays.stream(phoneNumbers)
                .map(phone -> SmsNotification.builder()
                        .recipient(phone)
                        .content(NotificationContent.builder()
                                .body("Flash sale! 50% off all items today only. Use code FLASH50")
                                .build())
                        .transactional(false) // Marketing message
                        .build())
                .toList();

        // Send in batch
        var results = sender.sendBatch((List<SmsNotification>) notifications);

        // Count successes and failures
        long successCount = results.stream().filter(NotificationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        System.out.println("   Total sent: " + results.size());
        System.out.println("   ✅ Successful: " + successCount);
        System.out.println("   ❌ Failed: " + failureCount);

        // Show details for each
        for (int i = 0; i < results.size(); i++) {
            NotificationResult result = results.get(i);
            if (result.isSuccess()) {
                NotificationResult.Success success = (NotificationResult.Success) result;
                System.out.println("   [" + (i+1) + "] ✅ " + success.recipient() + " - " +
                        success.providerMessageId().substring(0, 10) + "...");
            } else {
                NotificationResult.Failure failure = (NotificationResult.Failure) result;
                System.out.println("   [" + (i+1) + "] ❌ " + failure.recipient() + " - " +
                        failure.errorCode());
            }
        }
    }
}
