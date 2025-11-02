package com.example.usermanagement.service;

import com.example.usermanagement.dto.RoleDto;
import com.example.usermanagement.entity.Role;
import com.example.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<RoleDto> getRoleById(Long id) {
        return roleRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<RoleDto> getRoleByName(String name) {
        return roleRepository.findByName(name)
                .map(this::convertToDto);
    }

    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new RuntimeException("Role already exists");
        }

        Role role = Role.builder()
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Created role: {}", savedRole.getName());
        return convertToDto(savedRole);
    }

    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Check name uniqueness if changed
        if (!role.getName().equals(roleDto.getName()) &&
                roleRepository.existsByName(roleDto.getName())) {
            throw new RuntimeException("Role name already exists");
        }

        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());

        Role updatedRole = roleRepository.save(role);
        log.info("Updated role: {}", updatedRole.getName());
        return convertToDto(updatedRole);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        roleRepository.delete(role);
        log.info("Deleted role with id: {}", id);
    }

    private RoleDto convertToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
