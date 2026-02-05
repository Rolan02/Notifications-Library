package com.company.notifications.core.model;

/**
 * Supported push notification platforms.
 */
public enum PushPlatform {

    IOS("ios"),
    ANDROID("android"),
    WEB("web"),
    ALL("all");

    private final String value;

    PushPlatform(String value) {
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
