package com.friperie.felana.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Notez l'absence volontaire d'un champ "role" dans ce DTO :
 * le rôle GERANT ne doit JAMAIS pouvoir être attribué via cet endpoint.
 * Le service qui traite cette requête force le rôle à VENDEUR côté serveur,
 * ce qui empêche toute élévation de privilège même si un utilisateur
 * malveillant modifiait le corps de la requête.
 */
public record RegisterVendeurRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password
) {
}
