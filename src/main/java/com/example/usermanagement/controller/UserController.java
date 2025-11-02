package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserDto;
import com.example.usermanagement.service.RoleService;
import com.example.usermanagement.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        log.debug("Listing all users");
        return "users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserDto());
        model.addAttribute("roles", roleService.getAllRoles());
        return "users/form";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("user") UserDto userDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            return "users/form";
        }

        try {
            userService.createUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            log.info("User created: {}", userDto.getUsername());
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error creating user", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.getAllRoles());
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserDto user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("isEdit", true);
        return "users/form";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") UserDto userDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("isEdit", true);
            return "users/form";
        }

        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            log.info("User updated: {}", userDto.getUsername());
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error updating user", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("isEdit", true);
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
            log.info("User deleted: {}", id);
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/roles/{roleId}/assign")
    public String assignRole(@PathVariable Long userId,
                             @PathVariable Long roleId,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.assignRoleToUser(userId, roleId);
            redirectAttributes.addFlashAttribute("successMessage", "Role assigned successfully");
        } catch (Exception e) {
            log.error("Error assigning role", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/" + userId + "/edit";
    }

    @PostMapping("/{userId}/roles/{roleId}/remove")
    public String removeRole(@PathVariable Long userId,
                             @PathVariable Long roleId,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.removeRoleFromUser(userId, roleId);
            redirectAttributes.addFlashAttribute("successMessage", "Role removed successfully");
        } catch (Exception e) {
            log.error("Error removing role", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/" + userId + "/edit";
    }
}
