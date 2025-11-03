package com.example.usermanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Diagnostic bean that logs application configuration at startup.
 * Helps identify configuration issues and active profiles.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupDiagnostics implements ApplicationRunner {

    private final Environment environment;
    private final SecurityProperties securityProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("APPLICATION STARTUP DIAGNOSTICS");
        log.info("========================================");

        // Show active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            log.info("Active Profiles: {}", Arrays.toString(activeProfiles));
        } else {
            log.info("Active Profiles: NONE (using default profile)");
        }

        // Show default profiles
        String[] defaultProfiles = environment.getDefaultProfiles();
        log.info("Default Profiles: {}", Arrays.toString(defaultProfiles));

        // Show authentication mode
        String authMode = environment.getProperty("app.security.auth-mode");
        log.info("Authentication Mode (from property): {}", authMode);
        log.info("Authentication Mode (from SecurityProperties): {}",
                securityProperties.getAuthMode());

        // Show mode evaluation
        if (securityProperties.isApplicationMode()) {
            log.info("Running in APPLICATION MODE (database authentication)");
        } else if (securityProperties.isKeycloakMode()) {
            log.info("Running in KEYCLOAK MODE (SSO authentication)");
        } else {
            log.warn("UNKNOWN MODE - check configuration!");
        }

        // Show Keycloak bean status
        boolean hasKeycloakProperties = environment.containsProperty("keycloak.admin.server-url");
        log.info("Keycloak Admin Properties Configured: {}", hasKeycloakProperties);

        log.info("========================================");
    }
}
