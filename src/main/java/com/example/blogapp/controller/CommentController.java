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
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    public CommentController(CommentService commentService, PostService postService, UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping
    public String createComment(@RequestParam String content,
                                @RequestParam Long postId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post invalide"));

        Comment comment = new Comment(content, currentUser, post);
        commentService.save(comment);

        return "redirect:/posts";
    }

    @PostMapping("/{id}/update")
    public String updateComment(@PathVariable Long id,
                                @RequestParam String content,
                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire invalide"));

        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }

        comment.setContent(content);
        commentService.save(comment);

        return "redirect:/posts";
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire invalide"));

        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Seul l'auteur du commentaire ou l'auteur du post peut supprimer
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
                !comment.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        commentService.deleteById(id);
        return "redirect:/posts";
    }
}