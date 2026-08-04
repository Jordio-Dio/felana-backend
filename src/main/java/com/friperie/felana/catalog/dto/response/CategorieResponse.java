package com.friperie.felana.catalog.dto.response;

import com.friperie.felana.catalog.domain.Categorie;

public record CategorieResponse(Long id, String nom, String description) {
    public static CategorieResponse from(Categorie categorie) {
        return new CategorieResponse(categorie.getId(), categorie.getNom(), categorie.getDescription());
    }
}