package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.exception.ValidationException;

import java.util.regex.Pattern;

/** Validacion basica de formato de email (RFC simplificado, suficiente para este alcance). */
public class EmailValidator implements NotificationValidator<EmailNotification> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void validate(EmailNotification notification) throws ValidationException {
        String recipient = notification.getRecipient();
        if (recipient == null || !EMAIL_PATTERN.matcher(recipient).matches()) {
            throw new ValidationException("Email de destinatario invalido: " + recipient);
        }
        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            throw new ValidationException("El subject del email no puede estar vacio");
        }
        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            throw new ValidationException("El body del email no puede estar vacio");
        }
    }
}
