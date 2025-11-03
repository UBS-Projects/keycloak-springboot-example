package com.example.usermanagement.service;

import com.example.usermanagement.config.SecurityProperties;
import com.example.usermanagement.dto.UserDto;
import com.example.usermanagement.entity.Role;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Autowired(required = false)
    private KeycloakAdminService keycloakAdminService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .enabled(userDto.getEnabled() != null ? userDto.getEnabled() : true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(new HashSet<>())
                .build();

        // Assign default role if no roles provided
        if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.addRole(userRole);
        } else {
            userDto.getRoles().forEach(roleDto -> {
                Role role = roleRepository.findById(roleDto.getId())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleDto.getId()));
                user.addRole(role);
            });
        }

        User savedUser = userRepository.save(user);
        log.info("Created user: {}", savedUser.getUsername());

        // Sync with Keycloak if in Keycloak mode
        if (securityProperties.isKeycloakMode() && keycloakAdminService != null) {
            try {
                keycloakAdminService.createUser(
                        savedUser.getUsername(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        userDto.getPassword()
                );
                log.info("User {} synced to Keycloak", savedUser.getUsername());
            } catch (Exception e) {
                log.error("Failed to sync user to Keycloak", e);
                // Don't fail the transaction, user is created locally
            }
        }

        return convertToDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check username uniqueness if changed
        if (!user.getUsername().equals(userDto.getUsername()) &&
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String oldUsername = user.getUsername();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEnabled(userDto.getEnabled());

        // Update password only if provided
        boolean passwordChanged = false;
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            passwordChanged = true;
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user: {}", updatedUser.getUsername());

        // Sync with Keycloak if in Keycloak mode
        if (securityProperties.isKeycloakMode() && keycloakAdminService != null) {
            try {
                keycloakAdminService.updateUser(
                        oldUsername,  // Use old username to find user in Keycloak
                        updatedUser.getEmail(),
                        updatedUser.getFirstName(),
                        updatedUser.getLastName()
                );

                // Update password if changed
                if (passwordChanged) {
                    keycloakAdminService.updateUserPassword(oldUsername, userDto.getPassword());
                }

                // Update enabled status
                keycloakAdminService.setUserEnabled(updatedUser.getUsername(), updatedUser.getEnabled());

                log.info("User {} synced to Keycloak", updatedUser.getUsername());
            } catch (Exception e) {
                log.error("Failed to sync user update to Keycloak", e);
                // Don't fail the transaction
            }
        }

        return convertToDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String username = user.getUsername();

        // Remove user from all roles
        new HashSet<>(user.getRoles()).forEach(user::removeRole);

        userRepository.delete(user);
        log.info("Deleted user with id: {}", id);

        // Sync with Keycloak if in Keycloak mode
        if (securityProperties.isKeycloakMode() && keycloakAdminService != null) {
            try {
                keycloakAdminService.deleteUser(username);
                log.info("User {} deleted from Keycloak", username);
            } catch (Exception e) {
                log.error("Failed to delete user from Keycloak", e);
                // Don't fail the transaction
            }
        }
    }

    public UserDto assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.addRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Assigned role {} to user {}", role.getName(), user.getUsername());
        return convertToDto(updatedUser);
    }

    public UserDto removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.removeRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
        return convertToDto(updatedUser);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> com.example.usermanagement.dto.RoleDto.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .description(role.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
