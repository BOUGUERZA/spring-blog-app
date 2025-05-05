package com.example.blogapp.service;

import com.example.blogapp.model.Post;
import com.example.blogapp.model.User;
import com.example.blogapp.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Méthodes existantes conservées inchangées
    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> findByUser(User user) {
        return postRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    // Nouvelle méthode ajoutée pour les commentaires
    public Optional<Post> findByIdWithComments(Long id) {
        return postRepository.findWithCommentsById(id);
    }

    // Méthode utilitaire pour charger les commentaires si nécessaire
    public Post loadComments(Post post) {
        return postRepository.findWithCommentsById(post.getId()).orElse(post);
    }
}