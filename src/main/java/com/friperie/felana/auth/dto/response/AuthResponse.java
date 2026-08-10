package com.friperie.felana.auth.dto.response;

import java.util.UUID;

/**
 * Réponse renvoyée après un login ou un refresh réussi.
 * On renvoie aussi le rôle : pratique pour que le frontend adapte
 * immédiatement son affichage (menu GERANT vs menu VENDEUR) sans appel
 * supplémentaire.
 */
public record AuthResponse(
        Long id, // Ajout de l'ID
        String accessToken,
        String refreshToken,
        String email,
        String name,
        String role,
        String tokenType) {
    public static AuthResponse of(Long id, String accessToken, String refreshToken, String email, String name, String role) {
        return new AuthResponse(id, accessToken, refreshToken, email, name, role, "Bearer");
    }
}
