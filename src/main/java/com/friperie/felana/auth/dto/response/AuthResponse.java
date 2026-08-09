package com.friperie.felana.auth.dto.response;

/**
 * Réponse renvoyée après un login ou un refresh réussi.
 * On renvoie aussi le rôle : pratique pour que le frontend adapte
 * immédiatement son affichage (menu GERANT vs menu VENDEUR) sans appel
 * supplémentaire.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String username,
        String role,
        String tokenType
) {
    public static AuthResponse of(String accessToken, String refreshToken, String email, String username, String role) {
        return new AuthResponse(accessToken, refreshToken, email, username, role, "Bearer");
    }
}
