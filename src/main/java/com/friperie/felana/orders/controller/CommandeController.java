package com.friperie.felana.orders.controller;

import com.friperie.felana.auth.domain.User;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.dto.request.CommandeCreateRequest;
import com.friperie.felana.orders.dto.request.StatutUpdateRequest;
import com.friperie.felana.orders.dto.response.CommandeResponse;
import com.friperie.felana.orders.service.CommandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/commandes")
@RequiredArgsConstructor
@Tag(name = "Commandes", description = "Passation et suivi des commandes/ventes")
public class CommandeController {

    private final CommandeService commandeService;

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Liste paginée de toutes les commandes")
    @GetMapping
    public ResponseEntity<Page<CommandeResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(commandeService.findAll(pageable).map(CommandeResponse::from));
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Historique des commandes d'un client")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<CommandeResponse>> findByClient(
            @PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(commandeService.findByClient(clientId, pageable).map(CommandeResponse::from));
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Détail d'une commande")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(CommandeResponse.from(commandeService.findEntityById(id)));
    }

    /**
     * @AuthenticationPrincipal injecte directement l'utilisateur connecté
     * (notre entité User implémente UserDetails, donc Spring Security sait
     * la fournir ici sans code supplémentaire), pour tracer quel vendeur a
     * enregistré la vente.
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Créer une commande avec ses lignes (décrémente le stock)")
    @PostMapping
    public ResponseEntity<CommandeResponse> create(
            @Valid @RequestBody CommandeCreateRequest request,
            @AuthenticationPrincipal User vendeurConnecte
    ) {
        Commande commande = commandeService.create(request, vendeurConnecte);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommandeResponse.from(commande));
    }

    /**
     * Changement de statut ouvert à VENDEUR ET GERANT (ex: marquer PAYEE ou
     * LIVREE). Si vous voulez restreindre l'ANNULATION au seul GERANT,
     * ajoutez la vérification dans le service (voir note ci-dessous).
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Mettre à jour le statut d'une commande")
    @PatchMapping("/{id}/statut")
    public ResponseEntity<CommandeResponse> updateStatut(
            @PathVariable Long id, @Valid @RequestBody StatutUpdateRequest request) {
        return ResponseEntity.ok(CommandeResponse.from(commandeService.updateStatut(id, request.statut())));
    }
}