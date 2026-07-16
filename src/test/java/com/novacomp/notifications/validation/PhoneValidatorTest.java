package com.novacomp.notifications.validation;

import com.novacomp.notifications.channel.sms.SmsNotification;
import com.novacomp.notifications.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneValidatorTest {

    private final PhoneValidator validator = new PhoneValidator();

    @Test
    void aceptaUnNumeroE164Valido() {
        SmsNotification sms = SmsNotification.builder()
                .recipient("+51987654321")
                .message("Hola")
                .build();

        assertDoesNotThrow(() -> validator.validate(sms));
    }

    @Test
    void rechazaNumeroSinPrefijoInternacional() {
        SmsNotification sms = SmsNotification.builder()
                .recipient("987654321")
                .message("Hola")
                .build();

        assertThrows(ValidationException.class, () -> validator.validate(sms));
    }

    @Test
    void rechazaMensajeVacio() {
        SmsNotification sms = SmsNotification.builder()
                .recipient("+51987654321")
                .message("")
                .build();

        assertThrows(ValidationException.class, () -> validator.validate(sms));
    }
}
