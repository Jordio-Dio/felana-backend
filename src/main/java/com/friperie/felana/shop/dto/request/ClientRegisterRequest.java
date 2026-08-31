package com.friperie.felana.shop.dto.request;

import jakarta.validation.constraints.Size;

public record ClientRegisterRequest(
    String nom,
    String prenom,
    String email,
    String telephone,
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password
) {
    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    public boolean hasTelephone() {
        return telephone != null && !telephone.isBlank();
    }
}
