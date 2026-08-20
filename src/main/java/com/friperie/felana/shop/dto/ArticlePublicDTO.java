package com.friperie.felana.shop.dto;

import java.math.BigDecimal;
import java.util.List;

import com.friperie.felana.catalog.domain.Article;

/**
 * Vue strictement publique d'un article : aucun coût, aucune marge, aucun
 * seuil d'alerte interne. Seul un booléen "disponible" indique le stock,
 * jamais la quantité exacte (donnée de gestion interne).
 */
public record ArticlePublicDTO(
    Long id,
    String reference,
    String nom,
    String description,
    BigDecimal prixVente,
    List<String> imageUrls,
    boolean disponible,
    String categorieNom
) {
    public static ArticlePublicDTO from(Article article) {
        return new ArticlePublicDTO(
            article.getId(),
            article.getReference(),
            article.getNom(),
            article.getDescription(),
            article.getPrixVente(),
            article.getImageUrls(),
            article.getQuantiteStock() > 0,
            article.getCategorie().getNom()
        );
    }
}
