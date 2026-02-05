package com.company.notifications.provider.email;

import com.company.notifications.core.model.EmailNotification;
import com.company.notifications.provider.NotificationProvider;

/**
 * Specialized interface for email providers.
 *
 * Implementations:
 * - MockSendGridProvider
 * - MockMailgunProvider
 * - etc.
 *
 * All email providers must implement this interface, making them interchangeable.
 */
public interface EmailProvider extends NotificationProvider<EmailNotification> {

    default boolean supportsAttachments() {
        return true;
    }

    default boolean supportsHtml() {
        return true;
    }

    default long getMaxAttachmentSize() {
        return 25 * 1024 * 1024; // 25 MB default (common limit)
    }
}
