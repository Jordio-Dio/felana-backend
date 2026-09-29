package com.friperie.felana.shop.controller;

import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.dto.response.CommandeResponse;
import com.friperie.felana.orders.repository.ClientRepository;
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
import org.springframework.security.core.Authentication;

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
    private final ClientRepository clientRepository;

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

    @PostMapping("/orders")
public ResponseEntity<PublicOrderResponse> createOrder(
        @Valid @RequestBody PublicOrderRequest request,
        Authentication authentication) {
    
    Client client = null;

    if (authentication != null 
            && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
        
        Object principal = authentication.getPrincipal();

        // Cas 1 : Si le principal est directement l'objet Client
        if (principal instanceof Client c) {
            client = c;
        } 
        // Cas 2 : Si le principal est un UserDetails (Spring Security)
        else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String identifier = userDetails.getUsername(); // Contient le téléphone ou username
            
            // Utilisez votre clientRepository injecté pour la recherche par téléphone
            client = clientRepository.findByTelephone(identifier).orElse(null);
        }
    }

    System.out.println("[ORDER-DEBUG] Client détecté : " + (client != null ? client.getId() : "Invité (Guest)"));

    // La commande s'effectue avec le client s'il est connecté, ou en mode invité s'il vaut null
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