package com.friperie.felana.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
    @NotBlank @Email String email
) {
    
}
