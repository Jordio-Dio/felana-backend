package com.friperie.felana.shop.dto.request;

/** identifiant = email OU téléphone, peu importe lequel le client utilise. */
public record ClientLoginRequest(String identifiant, String password) {
}