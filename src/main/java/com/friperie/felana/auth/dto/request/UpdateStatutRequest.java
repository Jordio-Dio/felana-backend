package com.friperie.felana.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStatutRequest(
        @NotNull(message = "Le statut (enabled) est obligatoire")
        Boolean enabled
) {
}