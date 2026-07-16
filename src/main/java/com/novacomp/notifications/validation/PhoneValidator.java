package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.exception.ValidationException;

import java.util.regex.Pattern;

/** Valida formato E.164 (+<codigo pais><numero>), estandar usado por Twilio y similares. */
public class PhoneValidator implements NotificationValidator<SmsNotification> {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    @Override
    public void validate(SmsNotification notification) throws ValidationException {
        String recipient = notification.getRecipient();
        if (recipient == null || !E164_PATTERN.matcher(recipient).matches()) {
            throw new ValidationException(
                    "Numero de telefono invalido (se espera formato E.164, ej. +51987654321): " + recipient);
        }
        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            throw new ValidationException("El mensaje SMS no puede estar vacio");
        }
        if (notification.getMessage().length() > 1600) {
            throw new ValidationException("El mensaje SMS excede el limite de 1600 caracteres");
        }
    }
}
