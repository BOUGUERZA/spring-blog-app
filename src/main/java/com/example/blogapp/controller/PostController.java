package com.example.blogapp.controller;

import com.example.blogapp.model.Comment;
import com.example.blogapp.model.Post;
import com.example.blogapp.model.User;
import com.example.blogapp.service.CommentService;
import com.example.blogapp.service.PostService;
import com.example.blogapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    public PostController(PostService postService,
                          UserService userService,
                          CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping
    public String showPosts(Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Charger les posts avec leurs commentaires
            List<Post> posts = postService.findAll().stream()
                    .map(post -> postService.findByIdWithComments(post.getId()).orElse(post))
                    .collect(Collectors.toList());

            model.addAttribute("posts", posts);
            model.addAttribute("newPost", new Post());
            model.addAttribute("newComment", new Comment());
            model.addAttribute("currentUser", currentUser);

            return "posts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String createPost(@ModelAttribute("newPost") Post post,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            // Vérifier si l'utilisateur est bloqué
            if (currentUser.isBlocked()) {
                // Rediriger vers la page des posts avec un message d'erreur
                model.addAttribute("error", "Votre compte est bloqué. Vous ne pouvez pas ajouter de posts.");
                
                // Recharger les posts pour l'affichage
                List<Post> posts = postService.findAll().stream()
                        .map(p -> postService.findByIdWithComments(p.getId()).orElse(p))
                        .collect(Collectors.toList());
                
                model.addAttribute("posts", posts);
                model.addAttribute("newPost", new Post());
                model.addAttribute("newComment", new Comment());
                model.addAttribute("currentUser", currentUser);
                
                return "posts";
            }

            post.setUser(currentUser);
            postService.save(post);

            return "redirect:/posts";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{id}/update")
    public String updatePost(@PathVariable Long id,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post invalide"));

        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce post");
        }

        post.setContent(content);
        postService.save(post);
        return "redirect:/posts";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post invalide"));

        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce post");
        }

        // Supprimer d'abord les commentaires associés
        commentService.deleteByPostId(id);
        // Puis supprimer le post
        postService.deleteById(id);

        return "redirect:/posts";
    }
}