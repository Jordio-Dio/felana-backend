package com.friperie.felana.auth.repository;

import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


/**
 * Accès aux données pour User.
 * JpaRepository nous donne déjà save(), findById(), findAll(), delete()...
 * On ajoute uniquement les méthodes "métier" dont on a besoin, en s'appuyant
 * sur la dérivation de requêtes (Spring Data génère le SQL depuis le nom
 * de la méthode).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse e-mail.
     * Utilisé pour la connexion (login) et le chargement dans UserDetailsService.
     */
    Optional<User> findByEmail(String email);
     /**
     * Vérifie si un utilisateur existe déjà avec l'adresse e-mail donnée.
     * Utilisé pour la validation lors de l'inscription (register).
     */
    boolean existsByEmail(String email);
     /**
     * Recherche un utilisateur par son nom d'utilisateur.
     * Utilisé pour la validation lors de l'inscription (register).
     */
    Optional<User> findByName(String name);

    boolean existsByName(String name);

    Page<User> findByRole(Role role, Pageable pageable);
}
