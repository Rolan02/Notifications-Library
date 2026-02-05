package test;

import com.company.notifications.config.NotificationService;
import com.company.notifications.core.exception.ConfigurationException;
import com.company.notifications.core.model.*;
import com.company.notifications.provider.email.EmailProvider;
import com.company.notifications.provider.push.PushProvider;
import com.company.notifications.provider.sms.SmsProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
public class NotificationServiceTest {

    @Test
    void shouldBuildServiceWithAllChannels() {
        // Given
        EmailProvider emailProvider = mock(EmailProvider.class);
        when(emailProvider.isConfigured()).thenReturn(true);

        SmsProvider smsProvider = mock(SmsProvider.class);
        when(smsProvider.isConfigured()).thenReturn(true);

        PushProvider pushProvider = mock(PushProvider.class);
        when(pushProvider.isConfigured()).thenReturn(true);

        // When
        NotificationService service = NotificationService.builder()
                .withEmailProvider(emailProvider)
                .withSmsProvider(smsProvider)
                .withPushProvider(pushProvider)
                .build();

        // Then
        assertThat(service).isNotNull();
        assertThat(service.isChannelAvailable(NotificationChannel.EMAIL)).isTrue();
        assertThat(service.isChannelAvailable(NotificationChannel.SMS)).isTrue();
        assertThat(service.isChannelAvailable(NotificationChannel.PUSH)).isTrue();
    }

    @Test
    void shouldBuildServiceWithSingleChannel() {
        // Given
        EmailProvider emailProvider = mock(EmailProvider.class);
        when(emailProvider.isConfigured()).thenReturn(true);

        // When
        NotificationService service = NotificationService.builder()
                .withEmailProvider(emailProvider)
                .build();

        // Then
        assertThat(service).isNotNull();
        assertThat(service.isChannelAvailable(NotificationChannel.EMAIL)).isTrue();
        assertThat(service.isChannelAvailable(NotificationChannel.SMS)).isFalse();
        assertThat(service.isChannelAvailable(NotificationChannel.PUSH)).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenNoChannelsConfigured() {
        // Then
        assertThatThrownBy(() -> NotificationService.builder().build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("At least one notification channel must be configured");
    }

    @Test
    void shouldThrowExceptionWhenProviderNotConfigured() {
        // Given
        EmailProvider unconfiguredProvider = mock(EmailProvider.class);
        when(unconfiguredProvider.isConfigured()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> NotificationService.builder()
                .withEmailProvider(unconfiguredProvider)
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Email provider is not properly configured");
    }
}
