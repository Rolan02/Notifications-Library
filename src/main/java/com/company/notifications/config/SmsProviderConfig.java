package com.company.notifications.config;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Configuration for SMS providers.
 * Contains credentials and settings specific to SMS sending.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsProviderConfig extends ProviderConfig {

    private String accountSid;
    private String authToken;
    private String apiSecret;
    private String apiBaseUrl;
    private String fromPhoneNumber;
    private String shortCode;

    @Builder.Default
    private boolean requestDeliveryStatus = true;

    private String statusCallbackUrl;
    private Double maxPricePerSms;

    @Builder.Default
    private Integer validityPeriod = 86400; // 24 hours

    @Builder.Default
    private boolean useTestCredentials = false;

    @Override
    public boolean isValid() {
        // Account SID is required
        if (accountSid == null || accountSid.isBlank()) {
            return false;
        }

        // Auth token is required
        if (authToken == null || authToken.isBlank()) {
            return false;
        }

        // Either fromPhoneNumber or shortCode is required
        if ((fromPhoneNumber == null || fromPhoneNumber.isBlank())
                && (shortCode == null || shortCode.isBlank())) {
            return false;
        }

        return true;
    }

    @Override
    public String getProviderType() {
        return "sms";
    }

    public String getAccountSidMasked() {
        if (accountSid == null || accountSid.length() < 8) {
            return "***";
        }
        return accountSid.substring(0, 4) + "..." + accountSid.substring(accountSid.length() - 4);
    }

    public String getAuthTokenMasked() {
        if (authToken == null || authToken.length() < 8) {
            return "***";
        }
        return authToken.substring(0, 4) + "..." + authToken.substring(authToken.length() - 4);
    }
}
