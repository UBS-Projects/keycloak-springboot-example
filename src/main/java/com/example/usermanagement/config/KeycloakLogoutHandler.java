package com.example.usermanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Custom logout handler to properly logout from Keycloak
 */
@Component
@Slf4j
public class KeycloakLogoutHandler implements LogoutHandler {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri:#{null}}")
    private String issuerUri;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (issuerUri == null || authentication == null) {
            return;
        }

        logoutFromKeycloak(request, response, authentication);
    }

    private void logoutFromKeycloak(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String idToken = null;

            // Extract ID token from OIDC user
            if (authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                idToken = oidcUser.getIdToken().getTokenValue();
            }

            // Build the logout URL
            String logoutUrl = UriComponentsBuilder
                    .fromUriString(issuerUri)
                    .path("/protocol/openid-connect/logout")
                    .queryParam("post_logout_redirect_uri", getBaseUrl(request))
                    .build()
                    .toUriString();

            // If we have an ID token, add it to the logout request
            if (idToken != null) {
                logoutUrl = UriComponentsBuilder
                        .fromUriString(logoutUrl)
                        .queryParam("id_token_hint", idToken)
                        .build()
                        .toUriString();
            }

            log.info("Redirecting to Keycloak logout: {}", logoutUrl);
            response.sendRedirect(logoutUrl);

        } catch (IOException e) {
            log.error("Error during Keycloak logout redirect", e);
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;

        return url;
    }
}
