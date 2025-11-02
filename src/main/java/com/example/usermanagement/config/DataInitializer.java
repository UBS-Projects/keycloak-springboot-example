package com.example.usermanagement.config;

import com.example.usermanagement.entity.Role;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    public void run(String... args) {
        // Only initialize data in application mode
        if (securityProperties.isApplicationMode()) {
            log.info("Initializing test data for application authentication mode...");
            initializeData();
        } else {
            log.info("Keycloak mode enabled - skipping local data initialization");
        }
    }

    private void initializeData() {
        // Create roles if they don't exist
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN", "Administrator role with full access");
        Role userRole = createRoleIfNotExists("ROLE_USER", "Standard user role with limited access");
        Role moderatorRole = createRoleIfNotExists("ROLE_MODERATOR", "Moderator role with elevated permissions");

        // Create admin user
        createUserIfNotExists(
                "admin",
                "admin123",
                "admin@example.com",
                "Admin",
                "User",
                adminRole
        );

        // Create regular user
        createUserIfNotExists(
                "user",
                "user123",
                "user@example.com",
                "Regular",
                "User",
                userRole
        );

        // Create moderator user
        createUserIfNotExists(
                "moderator",
                "mod123",
                "moderator@example.com",
                "Moderator",
                "User",
                moderatorRole,
                userRole
        );

        // Create additional test users
        createUserIfNotExists(
                "john.doe",
                "password123",
                "john.doe@example.com",
                "John",
                "Doe",
                userRole
        );

        createUserIfNotExists(
                "jane.smith",
                "password123",
                "jane.smith@example.com",
                "Jane",
                "Smith",
                userRole
        );

        log.info("Test data initialization completed successfully!");
        log.info("=================================================");
        log.info("Test Users Created:");
        log.info("  Admin: username=admin, password=admin123");
        log.info("  User: username=user, password=user123");
        log.info("  Moderator: username=moderator, password=mod123");
        log.info("  User: username=john.doe, password=password123");
        log.info("  User: username=jane.smith, password=password123");
        log.info("=================================================");
    }

    private Role createRoleIfNotExists(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description(description)
                            .users(new HashSet<>())
                            .build();
                    Role savedRole = roleRepository.save(role);
                    log.info("Created role: {}", roleName);
                    return savedRole;
                });
    }

    private void createUserIfNotExists(String username, String password, String email,
                                       String firstName, String lastName, Role... roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>())
                    .build();

            for (Role role : roles) {
                user.addRole(role);
            }

            userRepository.save(user);
            log.info("Created user: {} with {} role(s)", username, roles.length);
        }
    }
}
