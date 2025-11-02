package com.example.usermanagement.controller;

import com.example.usermanagement.dto.RoleDto;
import com.example.usermanagement.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        log.debug("Listing all roles");
        return "roles/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("role", new RoleDto());
        return "roles/form";
    }

    @PostMapping
    public String createRole(@Valid @ModelAttribute("role") RoleDto roleDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "roles/form";
        }

        try {
            roleService.createRole(roleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Role created successfully");
            log.info("Role created: {}", roleDto.getName());
            return "redirect:/roles";
        } catch (Exception e) {
            log.error("Error creating role", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "roles/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoleDto role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        model.addAttribute("isEdit", true);
        return "roles/form";
    }

    @PostMapping("/{id}")
    public String updateRole(@PathVariable Long id,
                             @Valid @ModelAttribute("role") RoleDto roleDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "roles/form";
        }

        try {
            roleService.updateRole(id, roleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully");
            log.info("Role updated: {}", roleDto.getName());
            return "redirect:/roles";
        } catch (Exception e) {
            log.error("Error updating role", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            return "roles/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Role deleted successfully");
            log.info("Role deleted: {}", id);
        } catch (Exception e) {
            log.error("Error deleting role", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/roles";
    }
}
