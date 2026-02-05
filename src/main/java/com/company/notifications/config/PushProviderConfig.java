package com.company.notifications.config;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Configuration for push notification providers.
 * Contains credentials and settings specific to push notifications.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PushProviderConfig extends ProviderConfig  {

    private String serverKey;
    private String projectId;
    private String serviceAccountJson;
    private String apiEndpoint;

    @Builder.Default
    private boolean useSandbox = false;

    @Builder.Default
    private Integer defaultTtl = 86400; // 24 hours

    @Builder.Default
    private String defaultPriority = "high";

    @Builder.Default
    private boolean enableAnalytics = true;

    @Builder.Default
    private boolean dryRun = false;

    private String restrictedPackageName;
    private String bundleId;

    @Builder.Default
    private boolean collapseNotifications = false;

    @Override
    public boolean isValid() {
        // Server key is required
        if (serverKey == null || serverKey.isBlank()) {
            return false;
        }

        // Either projectId or serviceAccountJson should be provided
        // (depends on authentication method)
        return (projectId != null && !projectId.isBlank())
                || (serviceAccountJson != null && !serviceAccountJson.isBlank());
    }

    @Override
    public String getProviderType() {
        return "push";
    }

    public String getServerKeyMasked() {
        if (serverKey == null || serverKey.length() < 8) {
            return "***";
        }
        return serverKey.substring(0, 4) + "..." + serverKey.substring(serverKey.length() - 4);
    }

    public boolean hasServiceAccount() {
        return serviceAccountJson != null && !serviceAccountJson.isBlank();
    }
}
