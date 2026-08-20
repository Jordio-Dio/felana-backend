package com.friperie.felana.catalog.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ArticleCreateRequest(
        String reference,

        @NotBlank(message = "Le nom est obligatoire") @Size(max = 150) String nom,

        @Size(max = 2000) String description,

        @NotNull(message = "Le prix de vente est obligatoire") @DecimalMin(value = "0.0", inclusive = true, message = "Le prix de vente doit être positif") BigDecimal prixVente,

        @NotNull(message = "Le coût des matières est obligatoire") @DecimalMin(value = "0.0", inclusive = true, message = "Le coût des matières doit être positif") BigDecimal coutMatiere,

        @NotNull(message = "Le coût des accessoires est obligatoire") @DecimalMin(value = "0.0", inclusive = true, message = "Le coût des accessoires doit être positif") BigDecimal coutAccessoire,

        @NotNull(message = "Le coût de main d'œuvre est obligatoire") @DecimalMin(value = "0.0", inclusive = true, message = "Le coût de main d'œuvre doit être positif") BigDecimal coutMainOeuvre,

        @DecimalMin(value = "0.0", inclusive = true, message = "Le pourcentage de marge doit être positif") BigDecimal pourcentageMarge,

        @NotNull(message = "La quantité en stock est obligatoire") @Min(value = 0, message = "La quantité en stock ne peut pas être négative") Integer quantiteStock,

        @Min(value = 0) Integer seuilAlerte,

        List<String> imageUrls,

        boolean publieVitrine,

        @NotNull(message = "La catégorie est obligatoire") Long categorieId) {
}