package com.friperie.felana.orders.controller;

import com.friperie.felana.auth.domain.User;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.dto.request.CommandeCreateRequest;
import com.friperie.felana.orders.dto.request.StatutUpdateRequest;
import com.friperie.felana.orders.dto.response.CommandeResponse;
import com.friperie.felana.orders.service.CommandeService;
import com.friperie.felana.orders.service.InvoiceService;

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

// Ajoudes imports pour les recus :
import com.friperie.felana.orders.dto.request.OrderHistoryFilterRequest;
import com.friperie.felana.orders.dto.response.InvoiceResponse;
import com.friperie.felana.orders.dto.response.NotificationCountResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;


@RestController
@RequestMapping("/commandes")
@RequiredArgsConstructor
@Tag(name = "Commandes", description = "Passation et suivi des commandes/ventes")
public class CommandeController {

    private final CommandeService commandeService;
    // Ajouter le champ (via constructeur, @RequiredArgsConstructor le gère
    // automatiquement) :
    private final InvoiceService invoiceService;


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
     *                          (notre entité User implémente UserDetails, donc
     *                          Spring Security sait
     *                          la fournir ici sans code supplémentaire), pour
     *                          tracer quel vendeur a
     *                          enregistré la vente.
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Créer une commande avec ses lignes (décrémente le stock)")
    @PostMapping
    public ResponseEntity<CommandeResponse> create(
            @Valid @RequestBody CommandeCreateRequest request,
            @AuthenticationPrincipal User vendeurConnecte) {
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

    /**
     * Reçu/facture au format JSON structuré, prêt à être imprimé ou converti
     * en PDF côté frontend.
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Récupérer le reçu/facture d'une commande")
    @GetMapping("/{id}/recu")
    public ResponseEntity<InvoiceResponse> getRecu(@PathVariable Long id) {
        Commande commande = commandeService.findEntityById(id);
        return ResponseEntity.ok(invoiceService.generate(commande));
    }

    /**
     * Historique paginé et filtrable. Le GERANT voit tout ; un VENDEUR ne
     * voit QUE ses propres ventes, quel que soit ce qu'il tente de passer
     * dans les filtres (voir CommandeService.search : vendeurIdForce écrase
     * filter.vendeurId() pour un VENDEUR).
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Historique des commandes avec filtres (date, statut, client, vendeur)")
    @GetMapping("/historique")
    public ResponseEntity<Page<CommandeResponse>> historique(
            OrderHistoryFilterRequest filter,
            Pageable pageable,
            Authentication authentication,
            @AuthenticationPrincipal User utilisateurConnecte) {
        boolean isGerant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_GERANT"));

        Long vendeurIdForce = isGerant ? null : utilisateurConnecte.getId();

        Page<CommandeResponse> result = commandeService
                .search(filter, vendeurIdForce, pageable)
                .map(CommandeResponse::from);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation
    @GetMapping("/notifications/count")
    public ResponseEntity<NotificationCountResponse> getNotifications(@RequestParam(required = false) String param) {
        return ResponseEntity.ok(new NotificationCountResponse(commandeService.countAttenteValidation()));
    }
    
}