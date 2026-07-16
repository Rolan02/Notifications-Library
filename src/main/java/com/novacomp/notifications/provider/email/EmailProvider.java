package com.novacomp.notifications.provider.email;

import com.novacomp.notifications.channel.email.EmailNotification;
import com.novacomp.notifications.provider.ProviderResponse;

/**
 * Puerto (Strategy) para proveedores de Email. Agregar un nuevo proveedor
 * (ej. Amazon SES) = crear una nueva clase que implemente esta interfaz,
 * SIN modificar ningun codigo existente (Open/Closed + Dependency Inversion:
 * el resto de la libreria depende de esta abstraccion, no de SendGrid/Mailgun).
 */
public interface EmailProvider {

    ProviderResponse send(EmailNotification notification);

    /** Nombre identificable del proveedor, util para logs/metricas. */
    String getProviderName();
}
