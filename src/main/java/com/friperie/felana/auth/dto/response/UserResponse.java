package com.friperie.felana.auth.dto.response;

import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;

import java.time.Instant;

/**
 * Vue publique d'un compte utilisateur. Ne contient JAMAIS le mot de passe,
 * même haché - ce champ n'existe simplement pas dans ce record.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        boolean enabled,
        boolean emailVerified,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}