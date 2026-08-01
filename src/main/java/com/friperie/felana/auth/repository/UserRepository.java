package com.friperie.felana.auth.repository;

import com.friperie.felana.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Accès aux données pour User.
 * JpaRepository nous donne déjà save(), findById(), findAll(), delete()...
 * On ajoute uniquement les méthodes "métier" dont on a besoin, en s'appuyant
 * sur la dérivation de requêtes (Spring Data génère le SQL depuis le nom
 * de la méthode).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
