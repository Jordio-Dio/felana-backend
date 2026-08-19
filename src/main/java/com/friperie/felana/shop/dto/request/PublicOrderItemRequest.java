package com.friperie.felana.shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PublicOrderItemRequest(
        @NotNull(message = "L'article est obligatoire") 
        Long articleId,

        @NotNull(message = "La quantité est obligatoire") 
        @Min(value = 1, message = "La quantité doit être au moins 1") 
        Integer quantite) 
{

}
