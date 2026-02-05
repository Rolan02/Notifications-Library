package com.company.notifications.util;

/**
 * Linear backoff strategy.
 * Delay increases linearly: baseDelay * attempt
 *
 * Example with baseDelay=1000ms:
 * - Attempt 1: 1000ms (1s)
 * - Attempt 2: 2000ms (2s)
 * - Attempt 3: 3000ms (3s)
 * - Attempt 4: 4000ms (4s)
 *
 * Good for: Predictable retry patterns, simple rate limiting
 */
public class LinearBackoffStrategy implements RetryStrategy {

    private final long maxDelayMs;

    /**
     * Creates linear backoff with no maximum delay
     */
    public LinearBackoffStrategy() {
        this(Long.MAX_VALUE);
    }

    /**
     * Creates linear backoff with maximum delay cap
     *
     * @param maxDelayMs Maximum delay cap in milliseconds
     */
    public LinearBackoffStrategy(long maxDelayMs) {
        if (maxDelayMs <= 0) {
            throw new IllegalArgumentException("Max delay must be positive");
        }
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public long calculateDelay(int attempt, long baseDelayMs) {
        if (attempt <= 0) {
            throw new IllegalArgumentException("Attempt must be positive");
        }

        // Calculate: baseDelay * attempt
        long delay = baseDelayMs * attempt;

        // Cap at max delay
        return Math.min(delay, maxDelayMs);
    }

    @Override
    public String getName() {
        return "LinearBackoff";
    }
}
