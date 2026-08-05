package com.friperie.felana.orders.dto.response;

import com.friperie.felana.orders.domain.Client;

public record ClientResponse(
        Long id, String nom, String prenom, String email, String telephone, String adresse
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(), client.getNom(), client.getPrenom(),
                client.getEmail(), client.getTelephone(), client.getAdresse()
        );
    }
}