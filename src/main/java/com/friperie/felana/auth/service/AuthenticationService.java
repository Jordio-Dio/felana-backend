package com.friperie.felana.auth.service;

import com.friperie.felana.auth.domain.OtpPurpose;
import com.friperie.felana.auth.domain.RefreshToken;
import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.dto.request.ForgotPasswordRequest;
import com.friperie.felana.auth.dto.request.LoginRequest;
import com.friperie.felana.auth.dto.request.RegisterVendeurRequest;
import com.friperie.felana.auth.dto.request.ResetPasswordRequest;
import com.friperie.felana.auth.dto.request.VerifyEmailRequest;
import com.friperie.felana.auth.dto.response.AuthResponse;
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
    private final OtpService otpService;

    /**
     * Login : délègue la vérification email/password à l'
     * AuthenticationManager (qui utilise notre DaoAuthenticationProvider,
     * lui-même configuré avec UserDetailsServiceImpl + BCryptPasswordEncoder).
     * Si les identifiants sont mauvais, une BadCredentialsException est levée
     * automatiquement -> Spring renverra un 401/403 (à gérer via un
     * 
     * @ExceptionHandler global si vous voulez un message JSON personnalisé).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authentification via l'email et le mot de passe
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Si on arrive ici, l'authentification a réussi (sinon une exception a été
        // levée avant).
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Utilisateur authentifié introuvable."));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken.getToken(), user.getEmail(),user.getUsername(), user.getRole().name());
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

        return AuthResponse.of(newAccessToken, newRefreshToken.getToken(),user.getEmail(), user.getUsername(), user.getRole().name());
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

        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException(
                    "Un compte existe déjà avec cette adresse e-mail.");
        }

        User vendeur = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.VENDEUR) // <-- forcé côté serveur, jamais lu depuis la requête
                .enabled(true)
                .build();

        userRepository.save(vendeur);
        otpService.generateAndSend(vendeur, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte avec cet email."));
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Cet email est déjà vérifié.");
        }
        try {   
            otpService.generateAndSend(user, OtpPurpose.EMAIL_VERIFICATION);
        } catch (org.springframework.mail.MailException e) {
            throw new IllegalStateException(
                    "Impossible d'envoyer l'email de vérification. Vérifiez la configuration SMTP ou réessayez plus tard.",
                    e);
        }
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte avec cet email."));
        otpService.verify(user, OtpPurpose.EMAIL_VERIFICATION, request.code());
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(user -> otpService.generateAndSend(user, OtpPurpose.PASSWORD_RESET));
        // Volontairement : on ne lève AUCUNE exception si l'email n'existe pas,
        // pour ne pas révéler quels emails sont enregistrés (énumération de comptes).
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Requête invalide."));
        otpService.verify(user, OtpPurpose.PASSWORD_RESET, request.code());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}