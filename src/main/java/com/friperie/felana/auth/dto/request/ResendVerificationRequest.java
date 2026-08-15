package com.friperie.felana.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
    @NotBlank(message = "L'email est obligatoire") 
    @Email (message = "L'email doit être valide")
    String email
) {
    
}
