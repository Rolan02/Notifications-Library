package com.novacomp.notifications.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEngineTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void reemplazaTodosLosPlaceholdersPresentes() {
        NotificationTemplate template = new NotificationTemplate(
                "saludo", "Hola {{nombre}}, tu saldo es S/ {{monto}}.");

        String resultado = engine.render(template, Map.of("nombre", "Ana", "monto", "100"));

        assertEquals("Hola Ana, tu saldo es S/ 100.", resultado);
    }

    @Test
    void dejaVacioUnPlaceholderSinValorProvisto() {
        NotificationTemplate template = new NotificationTemplate("saludo", "Hola {{nombre}}!");

        String resultado = engine.render(template, Map.of());

        assertEquals("Hola !", resultado);
    }
}
