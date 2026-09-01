package com.friperie.felana.shop.dto.request;

import java.util.List;

import com.friperie.felana.shop.domain.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PublicOrderRequest (
        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement,

        @NotEmpty(message = "La commande doit contenir au moins un article")
        @Valid
        List<PublicOrderItemRequest> items
)  {
}
