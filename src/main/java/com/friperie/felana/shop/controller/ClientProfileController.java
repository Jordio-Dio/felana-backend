package com.friperie.felana.shop.controller;

import com.friperie.felana.auth.dto.request.ChangePasswordRequest;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.dto.response.ClientResponse;
import com.friperie.felana.shop.dto.request.ClientUpdateProfileRequest;
import com.friperie.felana.shop.service.ClientAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/public/client/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Mon profil (client)", description = "Gestion du profil du client connecté sur la vitrine")
public class ClientProfileController {

    private final ClientAuthService clientAuthService;

    @Operation(summary = "Voir mon profil")
    @GetMapping
    public ResponseEntity<ClientResponse> me(@AuthenticationPrincipal Client client) {
        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @Operation(summary = "Modifier mon profil")
    @PatchMapping
    public ResponseEntity<ClientResponse> updateProfile(
            @AuthenticationPrincipal Client client,
            @Valid @RequestBody ClientUpdateProfileRequest request) {
        return ResponseEntity.ok(clientAuthService.updateProfile(client.getId(), request));
    }

    @Operation(summary = "Changer mon mot de passe")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Client client,
            @Valid @RequestBody ChangePasswordRequest request) {
        clientAuthService.changePassword(client.getId(), request);
        return ResponseEntity.noContent().build();
    }
}