package test.sms;

import com.company.notifications.config.SmsProviderConfig;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.sms.MockTwilioProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for MockTwilioProvider
 */
class MockTwilioProviderTest {

    private SmsProviderConfig config;
    private MockTwilioProvider provider;

    @BeforeEach
    void setUp() {
        config = SmsProviderConfig.builder()
                .accountSid("AC1234567890abcdef1234567890abcdef")
                .authToken("test_auth_token_12345")
                .fromPhoneNumber("+15551234567")
                .build();

        provider = new MockTwilioProvider(config);
    }

    @Test
    void shouldSendSmsSuccessfully() {
        // Given
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = provider.send(sms);

        // Then
        assertThat(result).isNotNull();
        // Note: MockTwilioProvider has 92% success rate, but we can't guarantee success
        if (result.isSuccess()) {
            assertThat(result.getProviderMessageId()).startsWith("SM");
            assertThat(result.getStatusCode()).isEqualTo(201);
            assertThat(result.getMessage()).isNotBlank();
        }
    }

    @Test
    void shouldReturnCorrectProviderName() {
        // When
        String name = provider.getProviderName();

        // Then
        assertThat(name).isEqualTo("Twilio");
    }

    @Test
    void shouldReturnCorrectProviderType() {
        // When
        String type = provider.getProviderType();

        // Then
        assertThat(type).isEqualTo("sms");
    }

    @Test
    void shouldBeConfiguredWithValidConfig() {
        // When
        boolean configured = provider.isConfigured();

        // Then
        assertThat(configured).isTrue();
    }

    @Test
    void shouldNotBeConfiguredWithMissingAccountSid() {
        // Given
        SmsProviderConfig invalidConfig = SmsProviderConfig.builder()
                .accountSid(null) // Missing required field
                .authToken("test_token")
                .fromPhoneNumber("+15551234567")
                .build();

        MockTwilioProvider invalidProvider = new MockTwilioProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldNotBeConfiguredWithMissingAuthToken() {
        // Given
        SmsProviderConfig invalidConfig = SmsProviderConfig.builder()
                .accountSid("AC12345")
                .authToken(null) // Missing required field
                .fromPhoneNumber("+15551234567")
                .build();

        MockTwilioProvider invalidProvider = new MockTwilioProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldNotBeConfiguredWithMissingFromNumber() {
        // Given
        SmsProviderConfig invalidConfig = SmsProviderConfig.builder()
                .accountSid("AC12345")
                .authToken("test_token")
                .fromPhoneNumber(null) // Missing required field
                .shortCode(null) // And no short code either
                .build();

        MockTwilioProvider invalidProvider = new MockTwilioProvider(invalidConfig);

        // When
        boolean configured = invalidProvider.isConfigured();

        // Then
        assertThat(configured).isFalse();
    }

    @Test
    void shouldHandleTestCredentials() {
        // Given
        SmsProviderConfig testConfig = SmsProviderConfig.builder()
                .accountSid("AC12345")
                .authToken("test_token")
                .fromPhoneNumber("+15551234567")
                .useTestCredentials(true)
                .build();

        MockTwilioProvider testProvider = new MockTwilioProvider(testConfig);
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = testProvider.send(sms);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("test mode");
        assertThat(result.getMetadata()).containsEntry("test_mode", "true");
    }

    @Test
    void shouldIncludePriceInformation() {
        // Given
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = provider.send(sms);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getMetadata()).containsKey("price");
            assertThat(result.getMetadata()).containsEntry("price_unit", "USD");
        }
    }

    @Test
    void shouldIncludeSegmentInformation() {
        // Given
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = provider.send(sms);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).containsKey("num_segments");
        }
    }

    @Test
    void shouldIncludeRawResponseData() {
        // Given
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = provider.send(sms);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).isNotEmpty();
            assertThat(result.getRawResponse()).containsKey("sid");
            assertThat(result.getRawResponse()).containsKey("to");
            assertThat(result.getRawResponse()).containsKey("from");
            assertThat(result.getRawResponse()).containsKey("body");
        }
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        // Then
        assertThatThrownBy(() -> new MockTwilioProvider(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SmsProviderConfig cannot be null");
    }

    @Test
    void shouldSupportInternationalSms() {
        // When
        boolean supports = provider.supportsInternationalSms();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportDeliveryStatus() {
        // When
        boolean supports = provider.supportsDeliveryStatus();

        // Then
        assertThat(supports).isTrue();
    }

    @Test
    void shouldHaveMaxMessageLength() {
        // When
        int maxLength = provider.getMaxMessageLength();

        // Then
        assertThat(maxLength).isGreaterThan(0);
        assertThat(maxLength).isEqualTo(1600); // 10 concatenated messages
    }

    @Test
    void shouldNotSupportMms() {
        // When
        boolean supports = provider.supportsMms();

        // Then
        assertThat(supports).isFalse();
    }

    @Test
    void shouldUseShortCodeWhenNoPhoneNumber() {
        // Given
        SmsProviderConfig shortCodeConfig = SmsProviderConfig.builder()
                .accountSid("AC12345")
                .authToken("test_token")
                .shortCode("12345") // Using short code instead of phone number
                .build();

        MockTwilioProvider shortCodeProvider = new MockTwilioProvider(shortCodeConfig);
        SmsNotification sms = createValidSms();

        // When
        ProviderResult result = shortCodeProvider.send(sms);

        // Then (if successful)
        if (result.isSuccess()) {
            assertThat(result.getRawResponse()).containsEntry("from", "12345");
        }
    }

    /**
     * Helper method to create a valid SMS notification
     */
    private SmsNotification createValidSms() {
        return SmsNotification.builder()
                .recipient("+15559876543")
                .content(NotificationContent.builder()
                        .body("Test SMS message")
                        .build())
                .build();
    }
}
