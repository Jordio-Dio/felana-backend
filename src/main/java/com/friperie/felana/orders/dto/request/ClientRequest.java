package com.friperie.felana.orders.dto.request;

import jakarta.validation.constraints.*;

public record ClientRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String nom,

        @Size(max = 100)
        String prenom,

        @Email(message = "Format d'email invalide")
        String email,

        @Size(max = 30)
        String telephone,

        @Size(max = 500)
        String adresse
) {
}