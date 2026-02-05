package com.company.notifications.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Email-specific notification.
 * Extends Notification with email-specific fields like CC, BCC, attachments, etc.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailNotification extends Notification {

    private String subject;
    private String fromEmail;
    private String fromName;

    @Builder.Default
    private List<String> cc = new ArrayList<>();

    @Builder.Default
    private List<String> bcc = new ArrayList<>();

    private String replyTo;
    private String htmlBody;
    private String plainTextBody;

    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean hasRequiredFields() {
        return super.hasRequiredFields()
                && subject != null && !subject.isBlank();
    }

    public void addCc(String email) {
        this.cc.add(email);
    }

    public void addBcc(String email) {
        this.bcc.add(email);
    }

    public void addAttachment(String attachment) {
        this.attachments.add(attachment);
    }
}
