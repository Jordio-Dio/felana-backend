package com.friperie.felana.shop.controller;

import com.friperie.felana.shop.dto.response.ClientAuthResponse;
import com.friperie.felana.shop.dto.request.ClientLoginRequest;
import com.friperie.felana.shop.dto.request.ClientRegisterRequest;
import com.friperie.felana.shop.service.ClientAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/public/client")
@RequiredArgsConstructor
@Tag(name = "Compte client", description = "Inscription et connexion des clients de la vitrine")
public class ClientAuthController {

    private final ClientAuthService clientAuthService;

    @Operation(summary = "Créer un compte client (email et/ou téléphone)")
    @PostMapping("/register")
    public ResponseEntity<ClientAuthResponse> register(@Valid @RequestBody ClientRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientAuthService.register(request));
    }

   @Operation(summary = "Connexion d'un client (email ou téléphone)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ClientLoginRequest request) {
        try {
            ClientAuthResponse response = clientAuthService.login(request);
            
            // On utilise .token() au lieu de .getAccessToken() car c'est un record Java
            if (response == null || response.token() == null || response.token().isBlank()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Numéro de téléphone ou mot de passe incorrect."));
            }

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Numéro de téléphone ou mot de passe incorrect."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }}