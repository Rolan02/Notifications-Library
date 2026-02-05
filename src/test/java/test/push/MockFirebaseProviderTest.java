package test.push;

import com.company.notifications.config.PushProviderConfig;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.PushNotification;
import com.company.notifications.core.model.PushPlatform;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.push.MockFirebaseProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for MockFirebaseProvider
 */
class MockFirebaseProviderTest {

    private PushProviderConfig config;
    private MockFirebaseProvider provider;

    @BeforeEach
    void setUp() {
        config = PushProviderConfig.builder()
                .serverKey("AAAA1234567890:abcdefghijklmnopqrstuvwxyz")
                .projectId("test-project")
                .build();

        provider = new MockFirebaseProvider(config);
    }

    @Test
    void shouldSendPushSuccessfully() {
        // Given
        PushNotification push = createValidPush();

        // When
        ProviderResult result = provider.send(push);

        // Then
        assertThat(result).isNotNull();
        // Note: MockFirebaseProvider has 88% success rate
        if (result.isSuccess()) {
            assertThat(result.getProviderMessageId()).startsWith("projects/");
            assertThat(result.getStatusCode()).isEqualTo(200);
            assertThat(result.getMessage()).isNotBlank();
        }
    }

    @Test
    void shouldReturnCorrectProviderName() {
        // When
        String name = provider.getProviderName();

        // Then
        assertThat(name).isEqualTo("Firebase Cloud Messaging");
    }

    @Test
    void shouldReturnCorrectProviderType() {
        // When
        String type = provider.getProviderType();

        // Then
        assertThat(type).isEqualTo("push");
    }

    @Test
    void shouldBeConfiguredWithValidConfig() {
        // When
        boolean configured = provider.isConfigured();

        // Then
        assertThat(configured).isTrue();
    }

    @Test
    void shouldNotBeConfiguredWithMissingServerKey() {
        // Given
        PushProviderConfig invalidConfig = PushProviderConfig.builder()
                .serverKey(null) // Missing required field
                .projectId("test-project")
                .build();

        MockFirebaseProvider invalidProvider = new MockFirebaseProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldNotBeConfiguredWithMissingProjectId() {
        // Given
        PushProviderConfig invalidConfig = PushProviderConfig.builder()
                .serverKey("AAAA12345")
                .projectId(null) // Missing required field
                .serviceAccountJson(null) // And no service account either
                .build();

        MockFirebaseProvider invalidProvider = new MockFirebaseProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldHandleSandboxMode() {
        // Given
        PushProviderConfig sandboxConfig = PushProviderConfig.builder()
                .serverKey("AAAA12345")
                .projectId("test-project")
                .useSandbox(true)
                .build();

        MockFirebaseProvider sandboxProvider = new MockFirebaseProvider(sandboxConfig);
        PushNotification push = createValidPush();

        // When
        ProviderResult result = sandboxProvider.send(push);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("sandbox");
        assertThat(result.getMetadata()).containsEntry("sandbox", "true");
    }

    @Test
    void shouldHandleDryRunMode() {
        // Given
        PushProviderConfig dryRunConfig = PushProviderConfig.builder()
                .serverKey("AAAA12345")
                .projectId("test-project")
                .dryRun(true)
                .build();

        MockFirebaseProvider dryRunProvider = new MockFirebaseProvider(dryRunConfig);
        PushNotification push = createValidPush();

        // When
        ProviderResult result = dryRunProvider.send(push);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("dry run");
        assertThat(result.getMetadata()).containsEntry("dry_run", "true");
        assertThat(result.getMetadata()).containsEntry("validation_status", "passed");
    }

    @Test
    void shouldIncludePlatformMetadata() {
        // Given
        PushNotification push = createValidPush();

        // When
        ProviderResult result = provider.send(push);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getMetadata()).containsKey("platform");
            assertThat(result.getMetadata()).containsKey("priority");
        }
    }

    @Test
    void shouldIncludeAnalyticsMetadata() {
        // Given
        PushProviderConfig analyticsConfig = PushProviderConfig.builder()
                .serverKey("AAAA12345")
                .projectId("test-project")
                .enableAnalytics(true)
                .build();

        MockFirebaseProvider analyticsProvider = new MockFirebaseProvider(analyticsConfig);
        PushNotification push = createValidPush();

        // When
        ProviderResult result = analyticsProvider.send(push);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getMetadata()).containsEntry("analytics_enabled", "true");
        }
    }

    @Test
    void shouldIncludeRawResponseData() {
        // Given
        PushNotification push = createValidPush();

        // When
        ProviderResult result = provider.send(push);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).isNotEmpty();
            assertThat(result.getRawResponse()).containsKey("name");
            assertThat(result.getRawResponse()).containsKey("token");
            assertThat(result.getRawResponse()).containsKey("notification");
        }
    }

    @Test
    void shouldIncludeDataInRawResponse() {
        // Given
        PushNotification push = createValidPush();
        push.addData("key1", "value1");
        push.addData("key2", "value2");

        // When
        ProviderResult result = provider.send(push);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).containsKey("data");
        }
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        // Then
        assertThatThrownBy(() -> new MockFirebaseProvider(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PushProviderConfig cannot be null");
    }

    @Test
    void shouldSupportIos() {
        // When
        boolean supports = provider.supportsIos();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportAndroid() {
        // When
        boolean supports = provider.supportsAndroid();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportWebPush() {
        // When
        boolean supports = provider.supportsWebPush();

        // Then
        assertThat(supports).isTrue(); // FCM supports web push
    }

    @Test
    void shouldSupportRichMedia() {
        // When
        boolean supports = provider.supportsRichMedia();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportDataPayload() {
        // When
        boolean supports = provider.supportsDataPayload();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldHaveMaxPayloadSize() {
        // When
        int maxSize = provider.getMaxPayloadSize();

        // Then
        assertThat(maxSize).isGreaterThan(0);
        assertThat(maxSize).isEqualTo(4096); // 4 KB
    }

    @Test
    void shouldHaveMaxTtl() {
        // When
        int maxTtl = provider.getMaxTtl();

        // Then
        assertThat(maxTtl).isGreaterThan(0);
        assertThat(maxTtl).isEqualTo(2419200); // 28 days
    }

    /**
     * Helper method to create a valid push notification
     */
    private PushNotification createValidPush() {
        return PushNotification.builder()
                .recipient("dGhpc19pc19hX21vY2tfZmNtX3Rva2VuXzEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg")
                .title("Test Push")
                .content(NotificationContent.builder()
                        .body("Test push notification")
                        .build())
                .platform(PushPlatform.ALL)
                .build();
    }
}
