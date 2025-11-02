package com.example.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private String authMode = "application"; // "application" or "keycloak"

    private JwtProperties jwt = new JwtProperties();

    @Data
    public static class JwtProperties {
        private String secret;
        private Long expiration;
    }

    public boolean isApplicationMode() {
        return "application".equalsIgnoreCase(authMode);
    }

    public boolean isKeycloakMode() {
        return "keycloak".equalsIgnoreCase(authMode);
    }
}
