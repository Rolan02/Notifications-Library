package com.company.notifications.util;

public interface RetryStrategy {

    /**
     * Calculates the delay before the next retry attempt
     *
     * @param attempt The current attempt number (1-based)
     * @param baseDelayMs The base delay in milliseconds
     * @return The delay in milliseconds before the next attempt
     */
    long calculateDelay(int attempt, long baseDelayMs);

    /**
     * Gets the strategy name
     */
    String getName();
}
