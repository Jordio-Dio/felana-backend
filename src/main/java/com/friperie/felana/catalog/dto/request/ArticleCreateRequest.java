package com.friperie.felana.catalog.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ArticleCreateRequest(
        String reference,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150)
        String nom,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Le prix de vente est obligatoire")
        @DecimalMin(value = "0.0", inclusive = true, message = "Le prix de vente doit être positif")
        BigDecimal prixVente,

        @NotNull(message = "Le coût d'achat est obligatoire")
        @DecimalMin(value = "0.0", inclusive = true, message = "Le coût d'achat doit être positif")
        BigDecimal coutAchat,

        @NotNull(message = "La quantité en stock est obligatoire")
        @Min(value = 0, message = "La quantité en stock ne peut pas être négative")
        Integer quantiteStock,

        @Min(value = 0)
        Integer seuilAlerte,

        String imageUrl,

        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId
) {
}