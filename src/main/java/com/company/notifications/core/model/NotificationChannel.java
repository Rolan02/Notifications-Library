package com.company.notifications.core.model;

public enum NotificationChannel {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push");

    private final String value;

    NotificationChannel(String value) {
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


