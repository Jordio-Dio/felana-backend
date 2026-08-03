// auth/dto/ForgotPasswordRequest.java
package com.friperie.felana.auth.dto.request;
import jakarta.validation.constraints.*;

public record ForgotPasswordRequest(
        @NotBlank @Email String email
) {}