package com.friperie.felana.shop.dto.request;

public record ClientUpdateProfileRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        String adresse
) {
    public boolean hasEmail() { return email != null && !email.isBlank(); }
    public boolean hasTelephone() { return telephone != null && !telephone.isBlank(); }
}