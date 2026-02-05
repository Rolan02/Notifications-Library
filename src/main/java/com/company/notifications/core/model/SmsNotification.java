package com.company.notifications.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * SMS-specific notification.
 * Extends Notification with SMS-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsNotification extends Notification {

    private String fromPhoneNumber;
    @Builder.Default
    private boolean transactional = true;
    private String statusCallbackUrl;
    private Double maxPrice;
    private Integer validityPeriod;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean hasRequiredFields() {
        // SMS requires: recipient phone number and message body
        // Subject is NOT required for SMS
        return getRecipient() != null && !getRecipient().isBlank()
                && getContent() != null
                && getContent().getBody() != null && !getContent().getBody().isBlank();
    }
}
