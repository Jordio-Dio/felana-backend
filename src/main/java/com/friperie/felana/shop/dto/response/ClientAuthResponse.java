package com.friperie.felana.shop.dto.response;

public record ClientAuthResponse(
        Long id,
        String token,
        String nom
) {}