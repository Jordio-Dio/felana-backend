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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ClientAuthResponse> login(@Valid @RequestBody ClientLoginRequest request) {
        return ResponseEntity.ok(clientAuthService.login(request));
    }
}
