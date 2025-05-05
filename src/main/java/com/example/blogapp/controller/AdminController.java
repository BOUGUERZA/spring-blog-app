package com.example.blogapp.controller;

import com.example.blogapp.model.User;
import com.example.blogapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("newUser", new User());
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addAttribute("error", "Nom d'utilisateur existe déjà");
            return "redirect:/admin/users";
        }
        userService.save(user);
        redirectAttributes.addAttribute("success", "Utilisateur créé avec succès");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteById(id);
        redirectAttributes.addAttribute("success", "Utilisateur supprimé");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur invalide"));

        if (!user.getUsername().equals(username) && userService.existsByUsername(username)) {
            redirectAttributes.addAttribute("error", "Nom d'utilisateur existe déjà");
            return "redirect:/admin/users";
        }

        user.setUsername(username);
        user.setRole(role);
        userService.save(user);

        redirectAttributes.addAttribute("success", "Utilisateur mis à jour");
        return "redirect:/admin/users";
    }
}