package com.friperie.felana.shop.controller;

import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.dto.response.CommandeResponse;
import com.friperie.felana.orders.service.CommandeService;
import com.friperie.felana.shop.dto.ArticlePublicDTO;
import com.friperie.felana.shop.dto.request.PublicOrderRequest;
import com.friperie.felana.shop.dto.response.PublicOrderResponse;
import com.friperie.felana.shop.service.PublicShopService;
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

/**
 * Endpoints PUBLICS, sans authentification. Toute la sécurité repose ici
 * sur la validation stricte des DTOs (jamais de coûts/marges exposés) et
 * sur PublicShopService, qui ne délègue qu'aux méthodes déjà existantes
 * d'ArticleService (aucune logique dupliquée).
 */
@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
@Tag(name = "Boutique en ligne", description = "Catalogue public et commandes anonymes (guest checkout)")
public class PublicCatalogController {

    private final PublicShopService publicShopService;
    private final CommandeService commandeService;

    @Operation(summary = "Liste paginée des articles actifs du catalogue public")
    @GetMapping("/articles")
    public ResponseEntity<Page<ArticlePublicDTO>> findArticles(Pageable pageable) {
        return ResponseEntity.ok(publicShopService.findArticles(pageable));
    }

    @Operation(summary = "Détail public d'un article")
    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticlePublicDTO> findArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(publicShopService.findArticleById(id));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/orders")
    public ResponseEntity<PublicOrderResponse> createOrder(@Valid @RequestBody PublicOrderRequest request,
            @AuthenticationPrincipal Client client) {
        System.out.println("[ORDER-DEBUG] client=" + client);
        if (client != null) {
            System.out.println("[ORDER-DEBUG] client.id=" + client.getId() + " class=" + client.getClass().getName());
        }
        if (client == null) {
            throw new IllegalStateException("Client non authentifié correctement");
        }
        PublicOrderResponse response = publicShopService.createOrder(request, client);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Historique des commandes du client connecté")
    @GetMapping("/mes-commandes")
    public ResponseEntity<Page<CommandeResponse>> mesCommandes(
            @AuthenticationPrincipal Client client, Pageable pageable) {
        Page<CommandeResponse> result = commandeService
                .findMesCommandes(client.getId(), pageable)
                .map(CommandeResponse::from);
        return ResponseEntity.ok(result);
    }
}