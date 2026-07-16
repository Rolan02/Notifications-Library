package com.novacomp.notifications.template;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Registro en memoria de plantillas disponibles, indexadas por id. */
public class TemplateRegistry {

    private final Map<String, NotificationTemplate> templates = new ConcurrentHashMap<>();

    public void register(NotificationTemplate template) {
        templates.put(template.getId(), template);
    }

    public Optional<NotificationTemplate> find(String id) {
        return Optional.ofNullable(templates.get(id));
    }
}
