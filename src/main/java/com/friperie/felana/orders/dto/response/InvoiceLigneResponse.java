package com.friperie.felana.orders.dto.response;

import com.friperie.felana.orders.domain.LigneCommande;

import java.math.BigDecimal;

public record InvoiceLigneResponse(
        String reference,
        String articleNom,
        Integer quantite,
        BigDecimal prixUnitaire,
        BigDecimal sousTotal
) {
    public static InvoiceLigneResponse from(LigneCommande ligne) {
        return new InvoiceLigneResponse(
                ligne.getArticle().getReference(),
                ligne.getArticle().getNom(),
                ligne.getQuantite(),
                ligne.getPrixUnitaire(),
                ligne.getSousTotal()
        );
    }
}