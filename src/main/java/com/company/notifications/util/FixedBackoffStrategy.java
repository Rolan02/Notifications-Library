package com.company.notifications.util;

/**
 * Fixed backoff strategy.
 * Delay is constant across all attempts.
 *
 * Example with baseDelay=1000ms:
 * - Attempt 1: 1000ms (1s)
 * - Attempt 2: 1000ms (1s)
 * - Attempt 3: 1000ms (1s)
 * - Attempt 4: 1000ms (1s)
 *
 * Good for: Simple retry logic, known stable retry windows
 */
public class FixedBackoffStrategy implements RetryStrategy {

    @Override
    public long calculateDelay(int attempt, long baseDelayMs) {
        if (attempt <= 0) {
            throw new IllegalArgumentException("Attempt must be positive");
        }

        // Always return base delay
        return baseDelayMs;
    }

    @Override
    public String getName() {
        return "FixedBackoff";
    }
}
