package com.friperie.felana.catalog.dto.response;

import com.friperie.felana.catalog.domain.Article;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue restreinte pour le VENDEUR : ni coutAchat, ni marge, ni seuilAlerte
 * (info de gestion interne). Le champ n'existe même pas dans ce record -
 * impossible de le sérialiser par erreur, contrairement à une approche par
 * @JsonIgnore sur une entité partagée.
 */
public record ArticleVendeurResponse(
        Long id,
        String reference,
        String nom,
        String description,
        BigDecimal prixVente,
        Integer quantiteStock,
        List<String> imageUrls,
        boolean actif,
        CategorieResponse categorie
) {
    public static ArticleVendeurResponse from(Article article) {
        return new ArticleVendeurResponse(
                article.getId(),
                article.getReference(),
                article.getNom(),
                article.getDescription(),
                article.getPrixVente(),
                article.getQuantiteStock(),
                article.getImageUrls(   ),
                article.isActif(),
                CategorieResponse.from(article.getCategorie())
        );
    }
}