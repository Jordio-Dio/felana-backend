package com.friperie.felana.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Représente un refresh token stocké en base.
 *
 * Pourquoi stocker le refresh token en base (contrairement à l'access token) ?
 * - Il vit longtemps (ex: 7 à 30 jours) : il faut pouvoir le révoquer
 *   (déconnexion, changement de mot de passe, compte désactivé par le GERANT).
 * - L'access token, lui, reste stateless et court (ex: 15 min) : on ne le
 *   stocke jamais en base, on se contente de vérifier sa signature/expiration.
 *
 * Chaque ligne correspond à une "session" de rafraîchissement pour un user.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Le token lui-même est un UUID aléatoire, pas un JWT : il n'a pas besoin d'être auto-porteur. */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    /** Permet une révocation explicite (logout) sans supprimer la ligne (traçabilité). */
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}
