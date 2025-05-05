package com.example.blogapp.repository;

import com.example.blogapp.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Méthode existante
    List<Post> findAllByOrderByCreatedAtDesc();

    // Méthode existante
    List<Post> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Nouvelle méthode pour charger un post avec ses commentaires et leurs auteurs
    @EntityGraph(attributePaths = {"user", "comments", "comments.user"})
    Optional<Post> findWithCommentsById(Long id);

    // Méthode existante conservée
    Optional<Post> findById(Long id);
}