package com.friperie.felana.catalog.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Identique à ArticleCreateRequest dans cette version : on autorise la
 * modification de tous les champs, y compris coutAchat/prixVente. Si vous
 * voulez un jour restreindre certains champs en modification (ex: la
 * référence ne doit plus changer une fois créée), c'est ce DTO qu'il faudra
 * réduire - il est volontairement séparé de Create pour ça.
 */
public record ArticleUpdateRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150)
        String nom,

        @Size(max = 2000)
        String description,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal prixVente,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal coutMatiere,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal coutAccessoire,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal coutMainOeuvre,


        @NotNull @Min(0)
        Integer quantiteStock,

        @Min(0)
        Integer seuilAlerte,

        String imageUrl,

        Boolean actif,

        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId
) {
}