package com.friperie.felana.shop.dto.request;

import java.util.List;

import com.friperie.felana.shop.domain.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicOrderRequest (
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150)
        String nomClient,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Size(max = 30)
        String telephone,

        @NotBlank(message = "L'adresse de livraison est obligatoire")
        @Size(max = 500)
        String adresseLivraison,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement,

        @NotEmpty(message = "La commande doit contenir au moins un article")
        @Valid
        List<PublicOrderItemRequest> items
)  {
}
