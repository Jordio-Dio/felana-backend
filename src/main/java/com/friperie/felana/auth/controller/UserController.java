package com.friperie.felana.auth.controller;

import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.dto.request.UpdateStatutRequest;
import com.friperie.felana.auth.dto.response.UserResponse;
import com.friperie.felana.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Gestion des comptes utilisateurs (vendeurs). Réservé exclusivement au
 * GERANT - un VENDEUR n'a jamais besoin de voir la liste des comptes.
 * La création reste sur /auth/register-vendeur (AuthController), ce
 * controller gère la CONSULTATION et l'ACTIVATION/DESACTIVATION.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des comptes vendeurs (GERANT uniquement)")
@PreAuthorize("hasRole('GERANT')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Liste paginée des comptes vendeurs")
    @GetMapping("/vendeurs")
    public ResponseEntity<Page<UserResponse>> findVendeurs(Pageable pageable) {
        return ResponseEntity.ok(userService.findVendeurs(pageable).map(UserResponse::from));
    }

    @Operation(summary = "Détail d'un compte utilisateur")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.findEntityById(id)));
    }

    @Operation(summary = "Activer ou désactiver un compte vendeur")
    @PatchMapping("/{id}/statut")
    public ResponseEntity<UserResponse> updateStatut(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatutRequest request,
            @AuthenticationPrincipal User utilisateurConnecte
    ) {
        User updated = userService.updateStatut(id, request.enabled(), utilisateurConnecte);
        return ResponseEntity.ok(UserResponse.from(updated));
    }
}