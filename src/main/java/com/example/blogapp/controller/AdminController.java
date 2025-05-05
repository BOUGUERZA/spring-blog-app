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
        try {
            // Vérification si l'utilisateur existe déjà
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Nom d'utilisateur existe déjà");
                return "redirect:/admin/users";
            }
            
            // Définir un rôle par défaut si aucun n'est spécifié
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }
            
            // Sauvegarder l'utilisateur
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès");
        } catch (Exception e) {
            // Log l'erreur et ajouter un message d'erreur
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String role,
                             @RequestParam(required = false) Boolean blocked,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur invalide"));

            if (!user.getUsername().equals(username) && userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Nom d'utilisateur existe déjà");
                return "redirect:/admin/users";
            }
            
            user.setUsername(username);
            user.setRole(role);
            // Gestion du blocage
            user.setBlocked(blocked != null && blocked);
            // Le mot de passe est géré dans le service
            
            userService.save(user);

            redirectAttributes.addFlashAttribute("success", "Utilisateur mis à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/toggle-block")
    public String toggleBlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur invalide"));
            
            // Ne pas permettre de bloquer un administrateur
            if ("ROLE_ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Impossible de bloquer un administrateur");
                return "redirect:/admin/users";
            }
            
            // Inverser l'état de blocage
            user.setBlocked(!user.isBlocked());
            userService.save(user);
            
            String message = user.isBlocked() ? 
                    "Utilisateur " + user.getUsername() + " a été bloqué" : 
                    "Utilisateur " + user.getUsername() + " a été débloqué";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}