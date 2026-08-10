package com.friperie.felana.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'enregistrement d'un vendeur.
 * Le rôle GERANT est exclu pour des raisons de sécurité.
 */
public record RegisterVendeurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
        String name,

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "Format de l'adresse e-mail invalide")
        @Size(max = 100, message = "L'adresse e-mail ne doit pas dépasser 100 caractères")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password
) {}