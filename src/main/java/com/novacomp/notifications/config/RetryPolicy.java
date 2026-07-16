package com.novacomp.notifications.config;

/**
 * Politica de reintentos con backoff exponencial, usada por
 * RetryableNotificationSender (patron Decorator).
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final double backoffMultiplier;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelayMillis = builder.initialDelayMillis;
        this.backoffMultiplier = builder.backoffMultiplier;
    }

    public static RetryPolicy defaultPolicy() {
        return RetryPolicy.builder().maxAttempts(3).initialDelayMillis(200).backoffMultiplier(2.0).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long delayForAttempt(int attemptNumber) {
        // attemptNumber empieza en 1 (primer reintento)
        return (long) (initialDelayMillis * Math.pow(backoffMultiplier, attemptNumber - 1));
    }

    public static final class Builder {
        private int maxAttempts = 3;
        private long initialDelayMillis = 200;
        private double backoffMultiplier = 2.0;

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialDelayMillis(long initialDelayMillis) {
            this.initialDelayMillis = initialDelayMillis;
            return this;
        }

        public Builder backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
