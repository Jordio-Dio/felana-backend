package com.friperie.felana.auth.service;

import com.friperie.felana.auth.domain.RefreshToken;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.exception.TokenRefreshException;
import com.friperie.felana.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service dédié au cycle de vie du refresh token : création, vérification,
 * suppression. Contrairement à l'access token (JWT stateless géré par
 * JwtService), le refresh token est un simple identifiant opaque (UUID)
 * persisté en base, ce qui permet de le révoquer à tout moment.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /** Durée de vie du refresh token en millisecondes (ex: 604800000 = 7 jours). */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Crée et persiste un nouveau refresh token pour l'utilisateur donné.
     * On ne réutilise jamais un token existant : chaque login génère un
     * nouveau refresh token (rotation), ce qui limite l'impact d'un vol.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Vérifie qu'un refresh token existe, n'est ni expiré ni révoqué.
     * Lève TokenRefreshException sinon (mappée en 401/403 côté contrôleur
     * ou gestionnaire d'exceptions global).
     */
    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token introuvable."));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token révoqué. Veuillez vous reconnecter.");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        return refreshToken;
    }

    /**
     * Rotation : à chaque utilisation du refresh token pour obtenir un nouvel
     * access token, on révoque l'ancien et on en émet un nouveau. Cela permet
     * de détecter un vol : si un ancien refresh token révoqué est réutilisé,
     * c'est le signe qu'il a été compromis.
     */
    @Transactional
    public RefreshToken rotate(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return createRefreshToken(oldToken.getUser());
    }
}
