package com.friperie.felana.auth.repository;

import com.friperie.felana.auth.domain.RefreshToken;
import com.friperie.felana.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Utile lors d'un nouveau login : on peut choisir de révoquer les anciens
     * refresh tokens d'un user (politique "un seul appareil connecté" par
     * exemple). Ici on ne l'active pas par défaut, mais la méthode est prête.
     */
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.user = :user and r.revoked = false")
    void revokeAllByUser(User user);
}
