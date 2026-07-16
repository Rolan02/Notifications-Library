package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @Test
    void aceptaUnEmailValido() {
        EmailNotification email = EmailNotification.builder()
                .recipient("usuario@dominio.com")
                .subject("Asunto")
                .message("Cuerpo del mensaje")
                .build();

        assertDoesNotThrow(() -> validator.validate(email));
    }

    @Test
    void rechazaUnEmailConFormatoInvalido() {
        EmailNotification email = EmailNotification.builder()
                .recipient("no-es-un-email")
                .subject("Asunto")
                .message("Cuerpo")
                .build();

        assertThrows(ValidationException.class, () -> validator.validate(email));
    }

    @Test
    void rechazaSubjectVacio() {
        EmailNotification email = EmailNotification.builder()
                .recipient("usuario@dominio.com")
                .subject("   ")
                .message("Cuerpo")
                .build();

        assertThrows(ValidationException.class, () -> validator.validate(email));
    }
}
