package com.company.notifications.core.exception;

/**
 * Exception thrown when there's a configuration error.
 * This is typically thrown during service initialization or setup.
 */
public class ConfigurationException extends NotificationException {

    private final String configKey;

    public ConfigurationException(String message) {
        super("CONFIGURATION_ERROR", message);
        this.configKey = null;
    }

    public ConfigurationException(String message, Throwable cause) {
        super("CONFIGURATION_ERROR", message, cause);
        this.configKey = null;
    }

    public ConfigurationException(String configKey, String message) {
        super("CONFIGURATION_ERROR",
                String.format("Configuration error for '%s': %s", configKey, message));
        this.configKey = configKey;
    }

    public ConfigurationException(String configKey, String message, Throwable cause) {
        super("CONFIGURATION_ERROR",
                String.format("Configuration error for '%s': %s", configKey, message),
                cause);
        this.configKey = configKey;
    }

    /**
     * Gets the configuration key that caused the error (if available)
     */
    public String getConfigKey() {
        return configKey;
    }
}
