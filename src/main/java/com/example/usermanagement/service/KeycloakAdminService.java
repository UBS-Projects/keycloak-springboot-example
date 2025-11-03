package com.example.usermanagement.service;

import com.example.usermanagement.config.KeycloakAdminProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Service to manage users in Keycloak via Admin API
 * This service is only active when running in Keycloak mode
 */
@Service
@ConditionalOnProperty(name = "app.security.auth-mode", havingValue = "keycloak", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final KeycloakAdminProperties properties;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Keycloak Admin Client...");

            // Validate properties are not null
            if (properties.getServerUrl() == null || properties.getServerUrl().isEmpty()) {
                throw new IllegalStateException(
                    "Keycloak Admin properties are not configured. " +
                    "Please ensure you are running with the correct profile (e.g., -Dspring.profiles.active=keycloak-v17) " +
                    "and that keycloak.admin.server-url is set in your configuration."
                );
            }

            log.info("Server URL: {}, Realm: {}, Client ID: {}",
                    properties.getServerUrl(), properties.getRealm(), properties.getClientId());

            keycloak = KeycloakBuilder.builder()
                    .serverUrl(properties.getServerUrl())
                    .realm("master") // Use master realm for admin authentication
                    .clientId(properties.getClientId())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .build();

            realmResource = keycloak.realm(properties.getRealm());
            log.info("Keycloak Admin Client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak Admin Client", e);
            throw new RuntimeException("Failed to initialize Keycloak Admin Client: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (keycloak != null) {
            keycloak.close();
            log.info("Keycloak Admin Client closed");
        }
    }

    /**
     * Create a user in Keycloak
     */
    public String createUser(String username, String email, String firstName, String lastName, String password) {
        try {
            log.info("Creating user in Keycloak: {}", username);

            UsersResource usersResource = realmResource.users();

            // Check if user already exists
            List<UserRepresentation> existingUsers = usersResource.search(username, true);
            if (!existingUsers.isEmpty()) {
                log.warn("User {} already exists in Keycloak", username);
                return existingUsers.get(0).getId();
            }

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation().getPath());
                log.info("User created in Keycloak with ID: {}", userId);

                // Set password
                if (password != null && !password.isEmpty()) {
                    setUserPassword(userId, password);
                }

                response.close();
                return userId;
            } else {
                String error = response.readEntity(String.class);
                response.close();
                log.error("Failed to create user in Keycloak: {} - {}", response.getStatus(), error);
                throw new RuntimeException("Failed to create user in Keycloak: " + error);
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Error creating user in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Update a user in Keycloak
     */
    public void updateUser(String username, String email, String firstName, String lastName) {
        try {
            log.info("Updating user in Keycloak: {}", username);

            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }

            UserRepresentation user = users.get(0);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);

            UserResource userResource = usersResource.get(user.getId());
            userResource.update(user);

            log.info("User {} updated in Keycloak", username);
        } catch (Exception e) {
            log.error("Error updating user in Keycloak", e);
            throw new RuntimeException("Error updating user in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a user from Keycloak
     */
    public void deleteUser(String username) {
        try {
            log.info("Deleting user from Keycloak: {}", username);

            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                log.warn("User {} not found in Keycloak, skipping delete", username);
                return;
            }

            UserRepresentation user = users.get(0);
            usersResource.delete(user.getId());

            log.info("User {} deleted from Keycloak", username);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak", e);
            throw new RuntimeException("Error deleting user from Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Set or update user password in Keycloak
     */
    public void setUserPassword(String userId, String password) {
        try {
            log.info("Setting password for user ID: {}", userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            UserResource userResource = realmResource.users().get(userId);
            userResource.resetPassword(credential);

            log.info("Password set successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error setting password in Keycloak", e);
            throw new RuntimeException("Error setting password in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Update user password by username
     */
    public void updateUserPassword(String username, String newPassword) {
        try {
            log.info("Updating password for user: {}", username);

            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }

            String userId = users.get(0).getId();
            setUserPassword(userId, newPassword);

            log.info("Password updated for user: {}", username);
        } catch (Exception e) {
            log.error("Error updating password in Keycloak", e);
            throw new RuntimeException("Error updating password in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Enable or disable a user in Keycloak
     */
    public void setUserEnabled(String username, boolean enabled) {
        try {
            log.info("Setting user {} enabled status to: {}", username, enabled);

            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }

            UserRepresentation user = users.get(0);
            user.setEnabled(enabled);

            UserResource userResource = usersResource.get(user.getId());
            userResource.update(user);

            log.info("User {} enabled status set to: {}", username, enabled);
        } catch (Exception e) {
            log.error("Error setting user enabled status in Keycloak", e);
            throw new RuntimeException("Error setting user enabled status in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Extract user ID from Location header
     */
    private String extractUserIdFromLocation(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
