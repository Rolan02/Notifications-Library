package test;

import com.company.notifications.util.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for RetryPolicy
 */
public class RetryPolicyTest {
    @Test
    void shouldSucceedOnFirstAttempt() throws Exception {
        // Given
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(10)
                .logRetries(false)
                .build();

        // When
        String result = policy.execute(() -> "success");

        // Then
        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldRetryAndSucceed() throws Exception {
        // Given
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(10)
                .logRetries(false)
                .build();

        AtomicInteger attempts = new AtomicInteger(0);

        // When
        String result = policy.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("Transient error");
            }
            return "success";
        });

        // Then
        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldFailAfterMaxAttempts() {
        // Given
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(10)
                .logRetries(false)
                .build();

        AtomicInteger attempts = new AtomicInteger(0);

        // Then
        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new RuntimeException("Persistent error");
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Persistent error");

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldUseExponentialBackoff() throws Exception {
        // Given
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(100)
                .strategy(new ExponentialBackoffStrategy())
                .logRetries(false)
                .build();

        long[] delays = new long[2];
        AtomicInteger attempts = new AtomicInteger(0);
        long[] startTimes = new long[3];

        // When
        try {
            policy.execute(() -> {
                int attempt = attempts.incrementAndGet();
                startTimes[attempt - 1] = System.currentTimeMillis();
                throw new RuntimeException("Error");
            });
        } catch (Exception e) {
            // Expected
        }

        // Calculate actual delays
        delays[0] = startTimes[1] - startTimes[0];
        delays[1] = startTimes[2] - startTimes[1];

        // Then - delays should approximately double
        // First delay: ~100ms, Second delay: ~200ms
        assertThat(delays[0]).isGreaterThanOrEqualTo(90).isLessThan(150);
        assertThat(delays[1]).isGreaterThanOrEqualTo(180).isLessThan(250);
    }

    @Test
    void shouldUseLinearBackoff() throws Exception {
        // Given
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .baseDelayMs(100)
                .strategy(new LinearBackoffStrategy())
                .logRetries(false)
                .build();

        // Verify strategy is set correctly
        assertThat(policy.getStrategy()).isInstanceOf(LinearBackoffStrategy.class);
    }

    @Test
    void shouldUseFixedBackoff() {
        // Given
        RetryStrategy strategy = new FixedBackoffStrategy();

        // When/Then
        assertThat(strategy.calculateDelay(1, 1000)).isEqualTo(1000);
        assertThat(strategy.calculateDelay(2, 1000)).isEqualTo(1000);
        assertThat(strategy.calculateDelay(3, 1000)).isEqualTo(1000);
    }
}
