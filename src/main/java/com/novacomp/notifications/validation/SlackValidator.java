package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.slack.SlackNotification;
import com.novacomp.notifications.exception.ValidationException;

/** Validacion basica de una notificacion Slack (canal destino + mensaje). */
public class SlackValidator implements NotificationValidator<SlackNotification> {

    @Override
    public void validate(SlackNotification notification) throws ValidationException {
        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            throw new ValidationException("El canal/webhook destino (recipient) es obligatorio para Slack");
        }
        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            throw new ValidationException("El mensaje de Slack no puede estar vacio");
        }
    }
}
