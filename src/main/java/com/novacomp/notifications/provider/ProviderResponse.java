package com.novacomp.notifications.provider;

/**
 * Respuesta cruda simulada de un proveedor externo, previo a ser traducida
 * a NotificationResult por el Sender correspondiente. Modela lo minimo que
 * cualquier API real (SendGrid, Twilio, FCM, Slack) devuelve: un id de
 * mensaje/tracking y, si aplica, un mensaje de error.
 */
public final class ProviderResponse {

    private final boolean success;
    private final String providerMessageId;
    private final String errorMessage;

    private ProviderResponse(boolean success, String providerMessageId, String errorMessage) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
    }

    public static ProviderResponse success(String providerMessageId) {
        return new ProviderResponse(true, providerMessageId, null);
    }

    public static ProviderResponse failure(String errorMessage) {
        return new ProviderResponse(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
