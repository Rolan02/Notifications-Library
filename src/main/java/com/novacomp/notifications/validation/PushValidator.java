package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.push.PushNotification;
import com.novacomp.notifications.exception.ValidationException;

/** Validacion basica de una notificacion Push (device token + titulo). */
public class PushValidator implements NotificationValidator<PushNotification> {

    @Override
    public void validate(PushNotification notification) throws ValidationException {
        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            throw new ValidationException("El device token (recipient) es obligatorio para Push");
        }
        if (notification.getTitle() == null || notification.getTitle().isBlank()) {
            throw new ValidationException("El title es obligatorio para Push");
        }
    }
}
