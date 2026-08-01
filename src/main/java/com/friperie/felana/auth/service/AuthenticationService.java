package com.friperie.felana.auth.service;

import com.friperie.felana.auth.domain.RefreshToken;
import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.dto.AuthResponse;
import com.friperie.felana.auth.dto.LoginRequest;
import com.friperie.felana.auth.dto.RegisterVendeurRequest;
import com.friperie.felana.auth.repository.UserRepository;
import com.friperie.felana.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service central de l'authentification. C'est ici que se trouve la
 * "logique métier" : le contrôleur reste fin et délègue tout ici.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    /**
     * Login : délègue la vérification username/password à l'
     * AuthenticationManager (qui utilise notre DaoAuthenticationProvider,
     * lui-même configuré avec UserDetailsServiceImpl + BCryptPasswordEncoder).
     * Si les identifiants sont mauvais, une BadCredentialsException est levée
     * automatiquement -> Spring renverra un 401/403 (à gérer via un
     * @ExceptionHandler global si vous voulez un message JSON personnalisé).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Si on arrive ici, l'authentification a réussi (sinon une exception a été levée avant).
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Utilisateur authentifié introuvable."));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    /**
     * Rafraîchit un access token à partir d'un refresh token valide.
     * On applique une rotation : l'ancien refresh token est révoqué et un
     * nouveau est émis, renvoyé dans la réponse pour que le client le
     * remplace côté stockage (ex: cookie httpOnly ou stockage sécurisé mobile).
     */
    @Transactional
    public AuthResponse refreshAccessToken(String requestRefreshToken) {
        RefreshToken currentToken = refreshTokenService.verifyAndGet(requestRefreshToken);
        User user = currentToken.getUser();

        RefreshToken newRefreshToken = refreshTokenService.rotate(currentToken);
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.of(newAccessToken, newRefreshToken.getToken(), user.getRole().name());
    }

    /**
     * Création d'un compte VENDEUR.
     *
     * Sécurité : cette méthode ne vérifie PAS elle-même le rôle de
     * l'appelant -- c'est le rôle de @PreAuthorize("hasRole('GERANT')") sur
     * le contrôleur (voir AuthController). Le service, lui, garantit une
     * seconde ligne de défense : le rôle du nouveau compte est TOUJOURS
     * forcé à VENDEUR ici, indépendamment de ce qui pourrait être envoyé
     * dans la requête (defense in depth).
     */
    @Transactional
    public void registerVendeur(RegisterVendeurRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DataIntegrityViolationException(
                    "Un compte existe déjà avec ce nom d'utilisateur.");
        }

        User vendeur = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.VENDEUR) // <-- forcé côté serveur, jamais lu depuis la requête
                .enabled(true)
                .build();

        userRepository.save(vendeur);
    }
}
