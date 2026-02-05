package com.company.notifications.util;

/**
 * Exponential backoff strategy.
 * Delay increases exponentially: baseDelay * 2^(attempt-1)
 *
 * Example with baseDelay=1000ms:
 * - Attempt 1: 1000ms (1s)
 * - Attempt 2: 2000ms (2s)
 * - Attempt 3: 4000ms (4s)
 * - Attempt 4: 8000ms (8s)
 *
 * Good for: Transient failures, rate limits
 */
public class ExponentialBackoffStrategy implements RetryStrategy {

    private final double multiplier;
    private final long maxDelayMs;

    /**
     * Creates exponential backoff with default multiplier (2.0)
     */
    public ExponentialBackoffStrategy() {
        this(2.0, Long.MAX_VALUE);
    }

    /**
     * Creates exponential backoff with custom multiplier
     *
     * @param multiplier The multiplier for each attempt (typically 2.0)
     * @param maxDelayMs Maximum delay cap in milliseconds
     */
    public ExponentialBackoffStrategy(double multiplier, long maxDelayMs) {
        if (multiplier <= 1.0) {
            throw new IllegalArgumentException("Multiplier must be greater than 1.0");
        }
        if (maxDelayMs <= 0) {
            throw new IllegalArgumentException("Max delay must be positive");
        }

        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public long calculateDelay(int attempt, long baseDelayMs) {
        if (attempt <= 0) {
            throw new IllegalArgumentException("Attempt must be positive");
        }

        // Calculate: baseDelay * multiplier^(attempt-1)
        long delay = (long) (baseDelayMs * Math.pow(multiplier, attempt - 1));

        // Cap at max delay
        return Math.min(delay, maxDelayMs);
    }

    @Override
    public String getName() {
        return "ExponentialBackoff(multiplier=" + multiplier + ")";
    }
}
