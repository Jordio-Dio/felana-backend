// auth/dto/ResetPasswordRequest.java
package com.friperie.felana.auth.dto.request;
import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String code,
        @NotBlank @Size(min = 8) String newPassword
) {}