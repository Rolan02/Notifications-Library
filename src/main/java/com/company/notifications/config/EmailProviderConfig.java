package com.company.notifications.config;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Configuration for email providers.
 * Contains credentials and settings specific to email sending.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailProviderConfig extends ProviderConfig {

    private String apiKey;
    private String apiSecret;
    private String apiBaseUrl;
    private String fromEmail;
    private String fromName;
    private String replyToEmail;

    @Builder.Default
    private boolean trackOpens = false;

    @Builder.Default
    private boolean trackClicks = false;

    @Builder.Default
    private boolean sandboxMode = false;

    @Override
    public boolean isValid() {
        // API key is required
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        // From email is required
        if (fromEmail == null || fromEmail.isBlank()) {
            return false;
        }

        return true;
    }

    @Override
    public String getProviderType() {
        return "email";
    }

    public String getApiKeyMasked() {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
