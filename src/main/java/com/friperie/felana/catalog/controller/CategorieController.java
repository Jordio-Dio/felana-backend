package com.friperie.felana.catalog.controller;

import com.friperie.felana.catalog.domain.Categorie;
import com.friperie.felana.catalog.dto.request.CategorieRequest;
import com.friperie.felana.catalog.dto.response.CategorieResponse;
import com.friperie.felana.catalog.service.CategorieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories", description = "Gestion des catégories d'articles")
public class CategorieController {

    private final CategorieService categorieService;

    /** Lecture accessible à GERANT et VENDEUR : la liste des catégories n'est pas sensible. */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @GetMapping
    @Operation(summary = "Récupère la liste de toutes les catégories d'articles", description = "Accessible aux rôles GERANT et VENDEUR")
    public ResponseEntity<List<CategorieResponse>> findAll() {
        return ResponseEntity.ok(categorieService.findAll());
    }

    @PreAuthorize("hasRole('GERANT')")
    @PostMapping
    @Operation(summary = "Crée une nouvelle catégorie d'articles", description = "Accessible uniquement au rôle GERANT")
    public ResponseEntity<CategorieResponse> create(@Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.create(request));
    }

    @PreAuthorize("hasRole('GERANT')")
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une catégorie d'articles existante", description = "Accessible uniquement au rôle GERANT")
    public ResponseEntity<CategorieResponse> update(
            @PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.update(id, request));
    }

    @PreAuthorize("hasRole('GERANT')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une catégorie d'articles existante", description = "Accessible uniquement au rôle GERANT")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categorieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}