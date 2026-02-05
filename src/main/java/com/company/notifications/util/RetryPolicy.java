package com.company.notifications.util;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Policy for retrying failed operations.
 *
 * Design Pattern: Strategy Pattern + Builder Pattern
 * - Configurable retry behavior
 * - Pluggable backoff strategies
 * - Customizable retry conditions
 *
 * Usage:
 * <pre>
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxAttempts(3)
 *     .baseDelayMs(1000)
 *     .strategy(new ExponentialBackoffStrategy())
 *     .retryOn(exception -> exception instanceof RateLimitException)
 *     .build();
 *
 * T result = policy.execute(() -> someOperation());
 * </pre>
 */
@Getter
@Builder
public class RetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);

    @Builder.Default
    private final int maxAttempts = 3;

    @Builder.Default
    private final long baseDelayMs = 1000;

    @Builder.Default
    private final RetryStrategy strategy = new ExponentialBackoffStrategy();

    @Builder.Default
    private final Predicate<Throwable> retryCondition = throwable -> true;

    @Builder.Default
    private final Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();

    @Builder.Default
    private final boolean logRetries = true;

    /**
     * Executes an operation with retry logic
     *
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws Exception if all retry attempts fail
     */
    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (logRetries && attempt > 1) {
                    logger.info("Retry attempt {}/{}", attempt, maxAttempts);
                }

                return operation.execute();

            } catch (Exception e) {
                lastException = e;

                boolean shouldRetry = shouldRetry(e, attempt);

                if (shouldRetry && attempt < maxAttempts) {
                    long delay = strategy.calculateDelay(attempt, baseDelayMs);

                    if (logRetries) {
                        logger.warn("Operation failed (attempt {}/{}): {}. Retrying in {}ms...",
                                attempt, maxAttempts, e.getMessage(), delay);
                    }

                    sleep(delay);
                } else {
                    if (logRetries) {
                        if (!shouldRetry) {
                            logger.error("Operation failed with non-retryable exception: {}",
                                    e.getMessage());
                        } else {
                            logger.error("Operation failed after {} attempts: {}",
                                    maxAttempts, e.getMessage());
                        }
                    }
                    break;
                }
            }
        }

        throw lastException;
    }

    /**
     * Determines if an exception should trigger a retry
     */
    private boolean shouldRetry(Throwable throwable, int attempt) {
        // Don't retry if we've exhausted attempts
        if (attempt >= maxAttempts) {
            return false;
        }

        // Check if exception type is in retryable list
        if (!retryableExceptions.isEmpty()) {
            boolean isRetryable = retryableExceptions.stream()
                    .anyMatch(exClass -> exClass.isInstance(throwable));

            if (!isRetryable) {
                return false;
            }
        }

        // Check custom retry condition
        return retryCondition.test(throwable);
    }

    /**
     * Sleeps for the specified duration
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Retry sleep interrupted");
        }
    }

    /**
     * Functional interface for retryable operations
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

}
