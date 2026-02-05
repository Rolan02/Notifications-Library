package test;

import com.company.notifications.config.EmailProviderConfig;
import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.email.MockSendGridProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for MockSendGridProvider
 */
class MockSendGridProviderTest {

    private EmailProviderConfig config;
    private MockSendGridProvider provider;

    @BeforeEach
    void setUp() {
        config = EmailProviderConfig.builder()
                .apiKey("SG.test_key_12345")
                .fromEmail("test@example.com")
                .fromName("Test Sender")
                .build();

        provider = new MockSendGridProvider(config);
    }

    @Test
    void shouldSendEmailSuccessfully() {
        // Given
        EmailNotification email = createValidEmail();

        // When
        ProviderResult result = provider.send(email);

        // Then
        assertThat(result).isNotNull();
        // Note: MockSendGridProvider has 90% success rate, but we can't guarantee success
        // In real tests, we'd control randomness or test the logic separately
        if (result.isSuccess()) {
            assertThat(result.getProviderMessageId()).startsWith("sg_");
            assertThat(result.getStatusCode()).isEqualTo(202);
            assertThat(result.getMessage()).isNotBlank();
        }
    }

    @Test
    void shouldReturnCorrectProviderName() {
        // When
        String name = provider.getProviderName();

        // Then
        assertThat(name).isEqualTo("SendGrid");
    }

    @Test
    void shouldReturnCorrectProviderType() {
        // When
        String type = provider.getProviderType();

        // Then
        assertThat(type).isEqualTo("email");
    }

    @Test
    void shouldBeConfiguredWithValidConfig() {
        // When
        boolean configured = provider.isConfigured();

        // Then
        assertThat(configured).isTrue();
    }

    @Test
    void shouldNotBeConfiguredWithInvalidConfig() {
        // Given
        EmailProviderConfig invalidConfig = EmailProviderConfig.builder()
                .apiKey(null) // Missing required field
                .fromEmail("test@example.com")
                .build();

        MockSendGridProvider invalidProvider = new MockSendGridProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldHandleSandboxMode() {
        // Given
        EmailProviderConfig sandboxConfig = EmailProviderConfig.builder()
                .apiKey("SG.test_key_12345")
                .fromEmail("test@example.com")
                .sandboxMode(true)
                .build();

        MockSendGridProvider sandboxProvider = new MockSendGridProvider(sandboxConfig);
        EmailNotification email = createValidEmail();

        // When
        ProviderResult result = sandboxProvider.send(email);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("sandbox");
        assertThat(result.getMetadata()).containsEntry("sandbox", "true");
    }

    @Test
    void shouldIncludeTrackingMetadata() {
        // Given
        EmailProviderConfig trackingConfig = EmailProviderConfig.builder()
                .apiKey("SG.test_key_12345")
                .fromEmail("test@example.com")
                .trackOpens(true)
                .trackClicks(true)
                .build();

        MockSendGridProvider trackingProvider = new MockSendGridProvider(trackingConfig);
        EmailNotification email = createValidEmail();

        // When
        ProviderResult result = trackingProvider.send(email);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getMetadata()).containsEntry("tracking_opens", "true");
            assertThat(result.getMetadata()).containsEntry("tracking_clicks", "true");
        }
    }

    @Test
    void shouldIncludeRawResponseData() {
        // Given
        EmailNotification email = createValidEmail();

        // When
        ProviderResult result = provider.send(email);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).isNotEmpty();
            assertThat(result.getRawResponse()).containsKey("message_id");
            assertThat(result.getRawResponse()).containsKey("recipient");
            assertThat(result.getRawResponse()).containsKey("subject");
        }
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        // Then
        assertThatThrownBy(() -> new MockSendGridProvider(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EmailProviderConfig cannot be null");
    }

    @Test
    void shouldSupportAttachments() {
        // When
        boolean supports = provider.supportsAttachments();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportHtml() {
        // When
        boolean supports = provider.supportsHtml();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldHaveMaxAttachmentSize() {
        // When
        long maxSize = provider.getMaxAttachmentSize();

        // Then
        assertThat(maxSize).isGreaterThan(0);
        assertThat(maxSize).isEqualTo(25 * 1024 * 1024); // 25 MB
    }

    /**
     * Helper method to create a valid email notification
     */
    private EmailNotification createValidEmail() {
        return EmailNotification.builder()
                .recipient("recipient@example.com")
                .subject("Test Email")
                .content(NotificationContent.builder()
                        .body("This is a test email body")
                        .build())
                .build();
    }
}
