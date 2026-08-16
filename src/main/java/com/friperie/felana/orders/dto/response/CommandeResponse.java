package com.friperie.felana.orders.dto.response;

import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.StatutCommande;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CommandeResponse(
        Long id,
        String reference,
        Instant dateCommande,
        StatutCommande statut,
        ClientResponse client,
        String vendeurNom,
        String remise,
        BigDecimal totalAchat,
        List<LigneCommandeResponse> lignes
) {
    public static CommandeResponse from(Commande commande) {
        return new CommandeResponse(
                commande.getId(),
                commande.getReference(),
                commande.getDateCommande(),
                commande.getStatut(),
                ClientResponse.from(commande.getClient()),
                commande.getVendeur().getName(),
                commande.getRemise().toString(),
                commande.getTotalAchat(),
                commande.getLignes().stream().map(LigneCommandeResponse::from).toList()
        );
    }
}