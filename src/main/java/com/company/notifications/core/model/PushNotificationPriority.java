package com.company.notifications.core.model;

/**
 * Priority levels specific to push notifications.
 * Affects how quickly the notification is delivered and whether it can wake the device.
 */
public enum PushNotificationPriority {

    LOW("low"),
    NORMAL("normal"),
    HIGH("high");

    private final String value;

    PushNotificationPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
