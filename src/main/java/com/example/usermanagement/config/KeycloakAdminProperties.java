package com.example.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Keycloak Admin Client.
 * Only loaded when auth-mode is set to "keycloak".
 *
 * This prevents property injection errors when running in Application mode.
 */
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
@ConditionalOnProperty(name = "app.security.auth-mode", havingValue = "keycloak", matchIfMissing = false)
@Data
public class KeycloakAdminProperties {

    /**
     * Keycloak server URL (e.g., http://localhost:8080)
     */
    private String serverUrl;

    /**
     * Keycloak realm name
     */
    private String realm;

    /**
     * Admin username for Keycloak Admin API
     */
    private String username;

    /**
     * Admin password for Keycloak Admin API
     */
    private String password;

    /**
     * Client ID for admin operations (default: admin-cli)
     */
    private String clientId = "admin-cli";
}
