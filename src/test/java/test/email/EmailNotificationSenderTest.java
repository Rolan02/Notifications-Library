package test.email;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.email.EmailNotificationSender;
import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.email.EmailProvider;
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
 * Unit tests for EmailNotificationSender
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private EmailProvider mockProvider;

    private NotificationSender<EmailNotification> sender;

    @BeforeEach
    void setUp() {
        // Configure mock provider as ready
        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("MockProvider");

        sender = new EmailNotificationSender(mockProvider);
    }

    @Test
    void shouldSendEmailSuccessfully() {
        // Given
        EmailNotification email = createValidEmail();

        ProviderResult providerResult = ProviderResult.success(
                "provider-msg-123",
                "Email sent successfully"
        );

        when(mockProvider.send(any(EmailNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();

        NotificationResult.Success success = (NotificationResult.Success) result;
        assertThat(success.providerMessageId()).isEqualTo("provider-msg-123");
        assertThat(success.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(success.recipient()).isEqualTo("test@example.com");

        verify(mockProvider).send(email);
    }

    @Test
    void shouldHandleProviderFailure() {
        // Given
        EmailNotification email = createValidEmail();

        ProviderResult providerResult = ProviderResult.failure(
                "PROVIDER_ERROR",
                "Provider service unavailable"
        );

        when(mockProvider.send(any(EmailNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(failure.errorMessage()).isEqualTo("Provider service unavailable");
        assertThat(failure.retryable()).isFalse();
    }

    @Test
    void shouldHandleRetryableFailure() {
        // Given
        EmailNotification email = createValidEmail();

        ProviderResult providerResult = ProviderResult.retryableFailure(
                "RATE_LIMIT",
                "Rate limit exceeded",
                null
        );

        when(mockProvider.send(any(EmailNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.retryable()).isTrue();
        assertThat(failure.errorCode()).isEqualTo("RATE_LIMIT");
    }

    @Test
    void shouldFailValidationForInvalidEmail() {
        // Given
        EmailNotification email = EmailNotification.builder()
                .recipient("invalid-email") // Invalid email format
                .subject("Test")
                .content(NotificationContent.builder()
                        .body("Test body")
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");

        // Provider should not be called if validation fails
        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldFailValidationForMissingSubject() {
        // Given
        EmailNotification email = EmailNotification.builder()
                .recipient("test@example.com")
                .subject(null) // Missing subject
                .content(NotificationContent.builder()
                        .body("Test body")
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(failure.errorMessage()).contains("subject");

        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldThrowExceptionWhenProviderIsNull() {
        // Then
        assertThatThrownBy(() -> new EmailNotificationSender(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EmailProvider cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenProviderNotConfigured() {
        // Given
        EmailProvider unconfiguredProvider = mock(EmailProvider.class);
        when(unconfiguredProvider.isConfigured()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> new EmailNotificationSender(unconfiguredProvider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not properly configured");
    }

    @Test
    void shouldReturnCorrectChannel() {
        // When
        NotificationChannel channel = sender.getChannel();

        // Then
        assertThat(channel).isEqualTo(NotificationChannel.EMAIL);
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
        EmailNotification email = createValidEmail();

        when(mockProvider.send(any())).thenThrow(new RuntimeException("Unexpected error"));

        // When
        NotificationResult result = sender.send(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(failure.errorMessage()).contains("Unexpected error");
    }

    /**
     * Helper method to create a valid email notification
     */
    private EmailNotification createValidEmail() {
        return EmailNotification.builder()
                .recipient("test@example.com")
                .subject("Test Subject")
                .content(NotificationContent.builder()
                        .body("Test body content")
                        .build())
                .build();
    }
}
