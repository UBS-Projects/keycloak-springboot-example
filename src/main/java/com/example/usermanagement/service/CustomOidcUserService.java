package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom OIDC User Service that loads user authorities from local database
 * instead of using Keycloak roles.
 *
 * This allows Keycloak to handle authentication (SSO) while the application
 * manages authorization (roles and permissions) locally.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Load the OIDC user from Keycloak
        OidcUser oidcUser = super.loadUser(userRequest);

        // Extract username from Keycloak token
        String username = oidcUser.getPreferredUsername();
        if (username == null) {
            username = oidcUser.getEmail();
        }

        log.info("Loading local authorities for Keycloak user: {}", username);

        // Load user from local database to get roles
        Set<GrantedAuthority> authorities = loadAuthoritiesFromDatabase(username);

        if (authorities.isEmpty()) {
            log.warn("User '{}' authenticated via Keycloak but not found in local database. No roles assigned.", username);
            // User exists in Keycloak but not in local DB - give them no roles
            // Alternatively, you could auto-create the user with default roles
        } else {
            log.info("Loaded {} authorities for user '{}': {}",
                    authorities.size(),
                    username,
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        }

        // Create new OidcUser with local authorities instead of Keycloak roles
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    /**
     * Load user authorities from local database
     */
    private Set<GrantedAuthority> loadAuthoritiesFromDatabase(String username) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Find user in local database
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && Boolean.TRUE.equals(user.getEnabled())) {
            // Convert user roles to Spring Security authorities
            authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toSet());

            log.debug("User '{}' found in local database with roles: {}",
                    username,
                    user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
        } else if (user != null && !Boolean.TRUE.equals(user.getEnabled())) {
            log.warn("User '{}' exists in local database but is disabled", username);
        } else {
            log.warn("User '{}' not found in local database", username);
        }

        return authorities;
    }
}
