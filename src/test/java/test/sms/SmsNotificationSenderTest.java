package test.sms;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.sms.SmsNotificationSender;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.SmsNotification;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.sms.SmsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SmsNotificationSender
 */
@ExtendWith(MockitoExtension.class)
class SmsNotificationSenderTest {

    @Mock
    private SmsProvider mockProvider;

    private NotificationSender<SmsNotification> sender;

    @BeforeEach
    void setUp() {
        // Configure mock provider as ready
        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("MockProvider");

        sender = new SmsNotificationSender(mockProvider);
    }

    @Test
    void shouldSendSmsSuccessfully() {
        // Given
        SmsNotification sms = createValidSms();

        ProviderResult providerResult = ProviderResult.success(
                "SM12345",
                "SMS queued for delivery"
        );

        when(mockProvider.send(any(SmsNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();

        NotificationResult.Success success = (NotificationResult.Success) result;
        assertThat(success.providerMessageId()).isEqualTo("SM12345");
        assertThat(success.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(success.recipient()).isEqualTo("+15551234567");

        verify(mockProvider).send(sms);
    }

    @Test
    void shouldHandleProviderFailure() {
        // Given
        SmsNotification sms = createValidSms();

        ProviderResult providerResult = ProviderResult.failure(
                "PROVIDER_ERROR",
                "SMS delivery failed"
        );

        when(mockProvider.send(any(SmsNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(failure.errorMessage()).isEqualTo("SMS delivery failed");
        assertThat(failure.retryable()).isFalse();
    }

    @Test
    void shouldHandleRetryableFailure() {
        // Given
        SmsNotification sms = createValidSms();

        ProviderResult providerResult = ProviderResult.retryableFailure(
                "RATE_LIMIT",
                "Rate limit exceeded",
                null
        );

        when(mockProvider.send(any(SmsNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.retryable()).isTrue();
        assertThat(failure.errorCode()).isEqualTo("RATE_LIMIT");
    }

    @Test
    void shouldFailValidationForInvalidPhoneNumber() {
        // Given
        SmsNotification sms = SmsNotification.builder()
                .recipient("invalid-phone") // Invalid phone format
                .content(NotificationContent.builder()
                        .body("Test message")
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");

        // Provider should not be called if validation fails
        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldFailValidationForMissingBody() {
        // Given
        SmsNotification sms = SmsNotification.builder()
                .recipient("+15551234567")
                .content(NotificationContent.builder()
                        .body(null) // Missing body
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");

        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldAcceptLongMessages() {
        // Given
        String longMessage = "A".repeat(500); // Long message, will be multiple segments

        SmsNotification sms = SmsNotification.builder()
                .recipient("+15551234567")
                .content(NotificationContent.builder()
                        .body(longMessage)
                        .build())
                .build();

        ProviderResult providerResult = ProviderResult.success("SM12345", "SMS sent");
        when(mockProvider.send(any(SmsNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(mockProvider).send(sms);
    }

    @Test
    void shouldThrowExceptionWhenProviderIsNull() {
        // Then
        assertThatThrownBy(() -> new SmsNotificationSender(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SmsProvider cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenProviderNotConfigured() {
        // Given
        SmsProvider unconfiguredProvider = mock(SmsProvider.class);
        when(unconfiguredProvider.isConfigured()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> new SmsNotificationSender(unconfiguredProvider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not properly configured");
    }

    @Test
    void shouldReturnCorrectChannel() {
        // When
        NotificationChannel channel = sender.getChannel();

        // Then
        assertThat(channel).isEqualTo(NotificationChannel.SMS);
    }

    @Test
    void shouldBeReadyWhenProviderIsConfigured() {
        // When
        boolean ready = sender.isReady();

        // Then
        assertThat(ready).isTrue();
    }

    @Test
    void shouldHandleUnexpectedException() {
        // Given
        SmsNotification sms = createValidSms();

        when(mockProvider.send(any())).thenThrow(new RuntimeException("Unexpected error"));

        // When
        NotificationResult result = sender.send(sms);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(failure.errorMessage()).contains("Unexpected error");
    }

    /**
     * Helper method to create a valid SMS notification
     */
    private SmsNotification createValidSms() {
        return SmsNotification.builder()
                .recipient("+15551234567")
                .content(NotificationContent.builder()
                        .body("Test SMS message")
                        .build())
                .build();
    }
}
