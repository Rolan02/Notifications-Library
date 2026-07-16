package com.novacomp.notifications.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de renderizado minimalista: reemplaza placeholders {{clave}} por su
 * valor en el mapa de variables. Deliberadamente simple (sin logica
 * condicional/loops) para mantener la libreria libre de dependencias de
 * un motor de plantillas pesado (ej. Freemarker/Thymeleaf) que la volveria
 * menos "agnostica".
 */
public class TemplateEngine {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    public String render(NotificationTemplate template, Map<String, String> variables) {
        Matcher matcher = PLACEHOLDER.matcher(template.getContent());
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
