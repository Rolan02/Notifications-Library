package com.company.notifications.core.model;

/**
 * Priority levels for notifications.
 * Can be used for queuing and delivery strategies.
 */
public enum NotificationPriority {

    LOW(1),
    NORMAL(5),
    HIGH(10),
    CRITICAL(15);

    private final int level;

    NotificationPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
