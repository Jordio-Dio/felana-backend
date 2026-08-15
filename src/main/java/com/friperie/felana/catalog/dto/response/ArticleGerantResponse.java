package com.friperie.felana.catalog.dto.response;

import com.friperie.felana.catalog.domain.Article;

import java.math.BigDecimal;

/**
 * Vue complète réservée au GERANT : inclut le coût d'achat, la marge
 * calculée et l'indicateur de stock bas.
 */
public record ArticleGerantResponse(
        Long id,
        String reference,
        String nom,
        String description,
        BigDecimal prixVente,
        BigDecimal coutMatiere,
        BigDecimal coutAccessoire,
        BigDecimal coutMainOeuvre,
        BigDecimal coutAchat,
        BigDecimal marge,
        Integer quantiteStock,
        Integer seuilAlerte,
        boolean stockBas,
        String imageUrl,
        boolean actif,
        CategorieResponse categorie) {
    public static ArticleGerantResponse from(Article article) {
        return new ArticleGerantResponse(
                article.getId(),
                article.getReference(),
                article.getNom(),
                article.getDescription(),
                article.getPrixVente(),
                article.getCoutMatiere(),
                article.getCoutAccessoire(),
                article.getCoutMainOeuvre(),
                article.getCoutAchat(),
                article.getMarge(),
                article.getQuantiteStock(),
                article.getSeuilAlerte(),
                article.isStockBas(),
                article.getImageUrl(),
                article.isActif(),
                CategorieResponse.from(article.getCategorie()));
    }
}