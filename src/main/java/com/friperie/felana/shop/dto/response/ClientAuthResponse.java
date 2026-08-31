package com.friperie.felana.shop.dto.response;
public record ClientAuthResponse(
        Long clientId,
        String accessToken,
        String nom,
        boolean emailVerifie
) {
}
        