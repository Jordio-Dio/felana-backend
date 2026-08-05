package com.friperie.felana.orders.dto.request;

import com.friperie.felana.orders.domain.StatutCommande;
import jakarta.validation.constraints.NotNull;

public record StatutUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutCommande statut
) {
}