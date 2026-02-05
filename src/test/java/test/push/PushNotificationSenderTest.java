package test.push;

import com.company.notifications.channel.NotificationSender;
import com.company.notifications.channel.push.PushNotificationSender;
import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.core.model.NotificationContent;
import com.company.notifications.core.model.NotificationResult;
import com.company.notifications.core.model.PushNotification;
import com.company.notifications.core.model.PushPlatform;
import com.company.notifications.provider.ProviderResult;
import com.company.notifications.provider.push.PushProvider;
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
 * Unit tests for PushNotificationSender
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationSenderTest {

    @Mock
    private PushProvider mockProvider;

    private NotificationSender<PushNotification> sender;

    @BeforeEach
    void setUp() {
        // Configure mock provider as ready
        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("MockProvider");

        sender = new PushNotificationSender(mockProvider);
    }

    @Test
    void shouldSendPushSuccessfully() {
        // Given
        PushNotification push = createValidPush();

        ProviderResult providerResult = ProviderResult.success(
                "projects/test/messages/abc123",
                "Push sent successfully"
        );

        when(mockProvider.send(any(PushNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();

        NotificationResult.Success success = (NotificationResult.Success) result;
        assertThat(success.providerMessageId()).isEqualTo("projects/test/messages/abc123");
        assertThat(success.channel()).isEqualTo(NotificationChannel.PUSH);

        verify(mockProvider).send(push);
    }

    @Test
    void shouldHandleProviderFailure() {
        // Given
        PushNotification push = createValidPush();

        ProviderResult providerResult = ProviderResult.failure(
                "PROVIDER_ERROR",
                "Push delivery failed"
        );

        when(mockProvider.send(any(PushNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(failure.errorMessage()).isEqualTo("Push delivery failed");
        assertThat(failure.retryable()).isFalse();
    }

    @Test
    void shouldHandleRetryableFailure() {
        // Given
        PushNotification push = createValidPush();

        ProviderResult providerResult = ProviderResult.retryableFailure(
                "QUOTA_EXCEEDED",
                "Quota exceeded",
                null
        );

        when(mockProvider.send(any(PushNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.retryable()).isTrue();
        assertThat(failure.errorCode()).isEqualTo("QUOTA_EXCEEDED");
    }

    @Test
    void shouldFailValidationForInvalidDeviceToken() {
        // Given
        PushNotification push = PushNotification.builder()
                .recipient("invalid") // Too short to be valid
                .title("Test")
                .content(NotificationContent.builder()
                        .body("Test body")
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");

        // Provider should not be called if validation fails
        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldFailValidationForMissingTitle() {
        // Given
        PushNotification push = PushNotification.builder()
                .recipient("valid_token_1234567890abcdef")
                .title(null) // Missing title
                .content(NotificationContent.builder()
                        .body("Test body")
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(failure.errorMessage()).contains("title");

        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldFailValidationForMissingBody() {
        // Given
        PushNotification push = PushNotification.builder()
                .recipient("valid_token_1234567890abcdef")
                .title("Test Title")
                .content(NotificationContent.builder()
                        .body(null) // Missing body
                        .build())
                .build();

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        verify(mockProvider, never()).send(any());
    }

    @Test
    void shouldAcceptPushWithDataPayload() {
        // Given
        PushNotification push = createValidPush();
        push.addData("key1", "value1");
        push.addData("key2", "value2");

        ProviderResult providerResult = ProviderResult.success("msg-123", "Sent");
        when(mockProvider.send(any(PushNotification.class))).thenReturn(providerResult);

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(mockProvider).send(push);
    }

    @Test
    void shouldThrowExceptionWhenProviderIsNull() {
        // Then
        assertThatThrownBy(() -> new PushNotificationSender(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PushProvider cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenProviderNotConfigured() {
        // Given
        PushProvider unconfiguredProvider = mock(PushProvider.class);
        when(unconfiguredProvider.isConfigured()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> new PushNotificationSender(unconfiguredProvider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not properly configured");
    }

    @Test
    void shouldReturnCorrectChannel() {
        // When
        NotificationChannel channel = sender.getChannel();

        // Then
        assertThat(channel).isEqualTo(NotificationChannel.PUSH);
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
        PushNotification push = createValidPush();

        when(mockProvider.send(any())).thenThrow(new RuntimeException("Unexpected error"));

        // When
        NotificationResult result = sender.send(push);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFailure()).isTrue();

        NotificationResult.Failure failure = (NotificationResult.Failure) result;
        assertThat(failure.errorCode()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(failure.errorMessage()).contains("Unexpected error");
    }

    /**
     * Helper method to create a valid push notification
     */
    private PushNotification createValidPush() {
        return PushNotification.builder()
                .recipient("valid_fcm_token_1234567890abcdefghijklmnopqrstuvwxyz")
                .title("Test Title")
                .content(NotificationContent.builder()
                        .body("Test body content")
                        .build())
                .platform(PushPlatform.ALL)
                .build();
    }
}
