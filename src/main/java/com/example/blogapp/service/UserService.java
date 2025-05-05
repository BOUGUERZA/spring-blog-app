package com.example.blogapp.service;

import com.example.blogapp.model.User;
import com.example.blogapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Gardez le préfixe ROLE_ intact
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole()) // Directement le rôle stocké (ROLE_ADMIN ou ROLE_USER)
                .build();
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        // Vérifier si le mot de passe est vide ou s'il s'agit d'une mise à jour d'utilisateur existant
        if (user.getId() != null) {
            // C'est une mise à jour d'un utilisateur existant
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null && (user.getPassword() == null || user.getPassword().isEmpty())) {
                // Conserver le mot de passe existant si aucun nouveau mot de passe n'est fourni
                user.setPassword(existingUser.getPassword());
            } else if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Encoder le nouveau mot de passe
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            // C'est un nouvel utilisateur, encoder le mot de passe
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}