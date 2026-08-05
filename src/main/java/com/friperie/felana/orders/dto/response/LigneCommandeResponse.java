package com.friperie.felana.orders.dto.response;

import com.friperie.felana.orders.domain.LigneCommande;

import java.math.BigDecimal;

public record LigneCommandeResponse(
        Long id, Long articleId, String articleNom, Integer quantite,
        BigDecimal prixUnitaire, BigDecimal sousTotal
) {
    public static LigneCommandeResponse from(LigneCommande ligne) {
        return new LigneCommandeResponse(
                ligne.getId(),
                ligne.getArticle().getId(),
                ligne.getArticle().getNom(),
                ligne.getQuantite(),
                ligne.getPrixUnitaire(),
                ligne.getSousTotal()
        );
    }
}