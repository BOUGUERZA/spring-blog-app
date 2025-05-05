package com.example.blogapp.controller;

import com.example.blogapp.model.User;
import com.example.blogapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Identifiants invalides");
        }
        if (logout != null) {
            model.addAttribute("message", "Déconnexion réussie");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return "redirect:/register?error";
        }
        user.setRole("ROLE_USER");
        userService.save(user);
        return "redirect:/login?registered";
    }
}