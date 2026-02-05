package test;

import com.company.notifications.core.model.NotificationChannel;
import com.company.notifications.event.NotificationEvent;
import com.company.notifications.event.NotificationEventListener;
import com.company.notifications.event.NotificationEventPublisher;
import com.company.notifications.event.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for NotificationEventPublisher
 */
public class NotificationEventPublisherTest {

    private NotificationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new NotificationEventPublisher();
    }

    @Test
    void shouldSubscribeListener() {
        // Given
        NotificationEventListener listener = event -> {};

        // When
        publisher.subscribe(listener);

        // Then
        assertThat(publisher.getListenerCount()).isEqualTo(1);
    }

    @Test
    void shouldUnsubscribeListener() {
        // Given
        NotificationEventListener listener = event -> {};
        publisher.subscribe(listener);

        // When
        publisher.unsubscribe(listener);

        // Then
        assertThat(publisher.getListenerCount()).isEqualTo(0);
    }

    @Test
    void shouldPublishEventToListener() {
        // Given
        List<NotificationEvent> receivedEvents = new ArrayList<>();
        publisher.subscribe(receivedEvents::add);

        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_SENT)
                .notificationId("test-123")
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .build();

        // When
        publisher.publish(event);

        // Then
        assertThat(receivedEvents).hasSize(1);
        assertThat(receivedEvents.get(0).getEventType()).isEqualTo(NotificationEventType.NOTIFICATION_SENT);
        assertThat(receivedEvents.get(0).getNotificationId()).isEqualTo("test-123");
    }

    @Test
    void shouldPublishEventToMultipleListeners() {
        // Given
        List<NotificationEvent> listener1Events = new ArrayList<>();
        List<NotificationEvent> listener2Events = new ArrayList<>();

        publisher.subscribe(listener1Events::add);
        publisher.subscribe(listener2Events::add);

        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_SENT)
                .notificationId("test-123")
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .build();

        // When
        publisher.publish(event);

        // Then
        assertThat(listener1Events).hasSize(1);
        assertThat(listener2Events).hasSize(1);
    }

    @Test
    void shouldHandleListenerException() {
        // Given
        List<NotificationEvent> goodListenerEvents = new ArrayList<>();

        publisher.subscribe(event -> {
            throw new RuntimeException("Listener error");
        });
        publisher.subscribe(goodListenerEvents::add);

        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEventType.NOTIFICATION_SENT)
                .notificationId("test-123")
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .build();

        // When
        publisher.publish(event); // Should not throw

        // Then - second listener should still receive event
        assertThat(goodListenerEvents).hasSize(1);
    }

    @Test
    void shouldClearAllListeners() {
        // Given
        publisher.subscribe(event -> {});
        publisher.subscribe(event -> {});

        // When
        publisher.clearListeners();

        // Then
        assertThat(publisher.getListenerCount()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionWhenSubscribingNull() {
        // Then
        assertThatThrownBy(() -> publisher.subscribe(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Listener cannot be null");
    }
}
