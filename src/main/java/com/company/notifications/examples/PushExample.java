package com.company.notifications.examples;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.push.PushNotificationSender;
import com.company.notifications.config.PushProviderConfig;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.PushNotification;
import com.company.notifications.core.model.PushNotificationPriority;
import com.company.notifications.core.model.PushPlatform;
import com.company.notifications.provider.push.MockFirebaseProvider;

import java.util.List;

/**
 * Example demonstrating how to send push notifications.
 *
 * This shows the complete flow:
 * 1. Configure provider (Firebase)
 * 2. Create sender
 * 3. Build notification
 * 4. Send and handle result
 */
public class PushExample {

    public static void main(String[] args) {
        System.out.println("=== Push Notification Example ===\n");

        // Example 1: Basic push notification
        basicPushExample();

        System.out.println("\n---\n");

        // Example 2: Push with image
        pushWithImageExample();

        System.out.println("\n---\n");

        // Example 3: Push with data payload
        pushWithDataExample();

        System.out.println("\n---\n");

        // Example 4: High priority push
        highPriorityPushExample();

        System.out.println("\n---\n");

        // Example 5: Platform-specific push
        platformSpecificPushExample();

        System.out.println("\n---\n");

        // Example 6: Batch push notifications
        batchPushExample();
    }

    /**
     * Example 1: Sending a basic push notification
     */
    private static void basicPushExample() {
        System.out.println("Example 1: Basic Push Notification");

        // Step 1: Configure the provider
        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .useSandbox(false)
                .enableAnalytics(true)
                .build();

        // Step 2: Create provider and sender
        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        // Step 3: Build the notification
        PushNotification push = PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("New Message")
                .content(NotificationContent.builder()
                        .body("You have a new message from Alice")
                        .build())
                .platform(PushPlatform.ALL)
                .build();

        // Step 4: Send and handle result
        NotificationResult result = sender.send(push);

        if (result.isSuccess()) {
            NotificationResult.Success success = (NotificationResult.Success) result;
            System.out.println("✅ Push sent successfully!");
            System.out.println("   Notification ID: " + success.notificationId());
            System.out.println("   Provider Message ID: " + success.providerMessageId());
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Push failed to send!");
            System.out.println("   Error Code: " + failure.errorCode());
            System.out.println("   Error Message: " + failure.errorMessage());
            System.out.println("   Retryable: " + failure.retryable());
        }
    }

    /**
     * Example 2: Push notification with image
     */
    private static void pushWithImageExample() {
        System.out.println("Example 2: Push with Image");

        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .build();

        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        PushNotification push = PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("Flash Sale! 🔥")
                .content(NotificationContent.builder()
                        .body("50% off on all electronics. Limited time offer!")
                        .build())
                .imageUrl("https://example.com/images/flash-sale-banner.jpg")
                .clickAction("https://example.com/flash-sale")
                .sound("default")
                .build();

        NotificationResult result = sender.send(push);

        if (result.isSuccess()) {
            System.out.println("✅ Rich push notification with image sent!");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 3: Push notification with custom data payload
     */
    private static void pushWithDataExample() {
        System.out.println("Example 3: Push with Data Payload");

        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .build();

        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        PushNotification push = PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("Order Update")
                .content(NotificationContent.builder()
                        .body("Your order #12345 has been shipped!")
                        .build())
                .build();

        // Add custom data
        push.addData("order_id", "12345");
        push.addData("tracking_number", "1Z999AA10123456784");
        push.addData("status", "shipped");
        push.addData("action", "view_order");

        NotificationResult result = sender.send(push);

        if (result.isSuccess()) {
            System.out.println("✅ Push with data payload sent!");
            System.out.println("   Data fields: order_id, tracking_number, status, action");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 4: High priority push notification
     */
    private static void highPriorityPushExample() {
        System.out.println("Example 4: High Priority Push");

        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .build();

        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        // High priority notification that can wake up device
        PushNotification push = PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("🚨 Security Alert")
                .content(NotificationContent.builder()
                        .body("New login from unknown device in New York")
                        .build())
                .priority(PushNotificationPriority.HIGH)
                .sound("alert")
                .badge(1)
                .ttl(300) // 5 minutes - urgent
                .build();

        NotificationResult result = sender.send(push);

        if (result.isSuccess()) {
            System.out.println("✅ High priority security alert sent!");
        } else {
            NotificationResult.Failure failure = (NotificationResult.Failure) result;
            System.out.println("❌ Failed: " + failure.errorMessage());
        }
    }

    /**
     * Example 5: Platform-specific push notifications
     */
    private static void platformSpecificPushExample() {
        System.out.println("Example 5: Platform-Specific Push");

        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .build();

        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        // iOS-specific notification
        PushNotification iosPush = PushNotification.builder()
                .recipient("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef") // APNS token
                .title("New Achievement! 🏆")
                .content(NotificationContent.builder()
                        .body("You've unlocked the 'Speed Demon' badge!")
                        .build())
                .platform(PushPlatform.IOS)
                .sound("achievement.caf")
                .badge(5)
                .category("achievement")
                .build();

        NotificationResult iosResult = sender.send(iosPush);

        if (iosResult.isSuccess()) {
            System.out.println("✅ iOS push sent!");
        }

        // Android-specific notification
        PushNotification androidPush = PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("Update Available")
                .content(NotificationContent.builder()
                        .body("Version 2.0 is now available. Tap to update.")
                        .build())
                .platform(PushPlatform.ANDROID)
                .clickAction("/update")
                .build();

        NotificationResult androidResult = sender.send(androidPush);

        if (androidResult.isSuccess()) {
            System.out.println("✅ Android push sent!");
        }
    }

    /**
     * Example 6: Sending push notifications in batch
     */
    private static void batchPushExample() {
        System.out.println("Example 6: Batch Push Notifications");

        PushProviderConfig config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("my-firebase-project")
                .build();

        MockFirebaseProvider provider = new MockFirebaseProvider(config);
        NotificationSender<PushNotification> sender = new PushNotificationSender(provider);

        // Create multiple push notifications
        var deviceTokens = new String[]{
                "token_user_1_abcdefghijklmnopqrstuvwxyz123456",
                "token_user_2_abcdefghijklmnopqrstuvwxyz123456",
                "token_user_3_abcdefghijklmnopqrstuvwxyz123456",
                "token_user_4_abcdefghijklmnopqrstuvwxyz123456",
                "token_user_5_abcdefghijklmnopqrstuvwxyz123456"
        };

        var notifications = java.util.Arrays.stream(deviceTokens)
                .map(token -> PushNotification.builder()
                        .recipient(token)
                        .title("Live Event Starting Soon!")
                        .content(NotificationContent.builder()
                                .body("Join now to watch the keynote presentation")
                                .build())
                        .imageUrl("https://example.com/images/event-banner.jpg")
                        .clickAction("app://live-event")
                        .priority(PushNotificationPriority.HIGH)
                        .build())
                .toList();

        // Send in batch
        var results = sender.sendBatch((List<PushNotification>) notifications);

        // Count successes and failures
        long successCount = results.stream().filter(NotificationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        System.out.println("   Total sent: " + results.size());
        System.out.println("   ✅ Successful: " + successCount);
        System.out.println("   ❌ Failed: " + failureCount);

        // Show details for each
        for (int i = 0; i < Math.min(results.size(), 5); i++) {
            NotificationResult result = results.get(i);
            if (result.isSuccess()) {
                NotificationResult.Success success = (NotificationResult.Success) result;
                String msgId = success.providerMessageId();
                String shortId = msgId.substring(msgId.lastIndexOf('/') + 1,
                        Math.min(msgId.lastIndexOf('/') + 9, msgId.length()));
                System.out.println("   [" + (i+1) + "] ✅ " + shortId + "...");
            } else {
                NotificationResult.Failure failure = (NotificationResult.Failure) result;
                System.out.println("   [" + (i+1) + "] ❌ " + failure.errorCode());
            }
        }
    }
}
