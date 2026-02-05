package com.company.notifications.event;

/**
 * Types of notification events that can be published.
 */
public enum NotificationEventType {

    NOTIFICATION_CREATED,
    NOTIFICATION_SENDING,
    NOTIFICATION_SENT,
    NOTIFICATION_DELIVERED,
    NOTIFICATION_FAILED,
    NOTIFICATION_RETRYING,
    NOTIFICATION_QUEUED
}
