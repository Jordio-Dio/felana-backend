package com.friperie.felana.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CommandeCreateRequest(
        @NotNull(message = "Le client est obligatoire")
        Long clientId,

        @NotEmpty(message = "La commande doit contenir au moins une ligne")
        @Valid
        List<LigneCommandeRequest> lignes,

        @DecimalMin(value = "0.0", inclusive = true, message = "La remise ne peut pas être négative")
        BigDecimal remise

) {
}