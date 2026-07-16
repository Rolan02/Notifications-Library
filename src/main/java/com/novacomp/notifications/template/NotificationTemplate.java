package com.novacomp.notifications.template;

import java.util.Objects;

/**
 * Plantilla de mensaje reutilizable. El contenido usa placeholders con la
 * sintaxis {{variable}}, resueltos por TemplateEngine.
 */
public final class NotificationTemplate {

    private final String id;
    private final String content;

    public NotificationTemplate(String id, String content) {
        this.id = Objects.requireNonNull(id);
        this.content = Objects.requireNonNull(content);
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
