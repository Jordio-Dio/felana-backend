package com.friperie.felana.auth.controller;

import com.friperie.felana.auth.dto.request.ForgotPasswordRequest;
import com.friperie.felana.auth.dto.request.LoginRequest;
import com.friperie.felana.auth.dto.request.RefreshTokenRequest;
import com.friperie.felana.auth.dto.request.RegisterVendeurRequest;
import com.friperie.felana.auth.dto.request.ResetPasswordRequest;
import com.friperie.felana.auth.dto.request.VerifyEmailRequest;
import com.friperie.felana.auth.dto.response.AuthResponse;
import com.friperie.felana.auth.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints pour l'authentification et la gestion des comptes.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint public (voir SecurityConfig : "/api/auth/login" est en
     * permitAll()). Renvoie un access token + un refresh token.
     */
    @Operation(summary = "Connexion utilisateurs", description = "Permet de se connecter avec email et mot de passe")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    /**
     * Endpoint public également : le client n'a besoin que d'un refresh
     * token valide (pas d'access token) pour en obtenir un nouveau.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.refreshToken()));
    }

    /**
     * Endpoint STRICTEMENT réservé au GERANT.
     *
     * @PreAuthorize("hasRole('GERANT')") s'exécute AVANT le corps de la
     * méthode : Spring intercepte l'appel, regarde les "authorities" de
     * l'utilisateur authentifié (posées par JwtAuthenticationFilter dans le
     * SecurityContext) et vérifie la présence de "ROLE_GERANT". Si absent,
     * une AccessDeniedException est levée -> HTTP 403 Forbidden, et le corps
     * de la méthode n'est JAMAIS exécuté.
     *
     * Cela fonctionne uniquement parce que :
     * 1. @EnableMethodSecurity(prePostEnabled = true) est actif (SecurityConfig).
     * 2. User.getAuthorities() renvoie bien "ROLE_GERANT" pour un compte GERANT.
     */

    // PreAuthorize("hasRole('GERANT')")
    @PreAuthorize("hasAuthority('ROLE_GERANT')")
    @PostMapping("/register-vendeur")

    public ResponseEntity<Void> registerVendeur(@Valid @RequestBody RegisterVendeurRequest request) {
        authenticationService.registerVendeur(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authenticationService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestParam String email) {
        authenticationService.resendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok().build(); // toujours 200, même si l'email n'existe pas
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
