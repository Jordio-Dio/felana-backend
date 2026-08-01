package com.friperie.felana.auth.controller;

import com.friperie.felana.auth.dto.AuthResponse;
import com.friperie.felana.auth.dto.LoginRequest;
import com.friperie.felana.auth.dto.RefreshTokenRequest;
import com.friperie.felana.auth.dto.RegisterVendeurRequest;
import com.friperie.felana.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint public (voir SecurityConfig : "/api/auth/login" est en
     * permitAll()). Renvoie un access token + un refresh token.
     */
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
     *  1. @EnableMethodSecurity(prePostEnabled = true) est actif (SecurityConfig).
     *  2. User.getAuthorities() renvoie bien "ROLE_GERANT" pour un compte GERANT.
     */
    @PreAuthorize("hasRole('GERANT')")
    @PostMapping("/register-vendeur")
    public ResponseEntity<Void> registerVendeur(@Valid @RequestBody RegisterVendeurRequest request) {
        authenticationService.registerVendeur(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
