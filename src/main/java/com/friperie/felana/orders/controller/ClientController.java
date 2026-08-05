package com.friperie.felana.orders.controller;

import com.friperie.felana.orders.dto.request.ClientRequest;
import com.friperie.felana.orders.dto.response.ClientResponse;
import com.friperie.felana.orders.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des fiches clients")
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Liste paginée des clients")
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(clientService.findAll(pageable).map(ClientResponse::from));
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Détail d'un client")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ClientResponse.from(clientService.findEntityById(id)));
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Créer une fiche client")
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClientResponse.from(clientService.create(request)));
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @Operation(summary = "Modifier une fiche client")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ClientResponse.from(clientService.update(id, request)));
    }

    @PreAuthorize("hasRole('GERANT')")
    @Operation(summary = "Supprimer un client (GERANT uniquement)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}