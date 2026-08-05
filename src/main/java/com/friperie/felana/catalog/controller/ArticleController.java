package com.friperie.felana.catalog.controller;

import com.friperie.felana.catalog.domain.Article;
import com.friperie.felana.catalog.dto.request.ArticleCreateRequest;
import com.friperie.felana.catalog.dto.request.ArticleUpdateRequest;
import com.friperie.felana.catalog.dto.response.ArticleGerantResponse;
import com.friperie.felana.catalog.dto.response.ArticleVendeurResponse;
import com.friperie.felana.catalog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * Un seul endpoint de lecture pour les deux rôles : la vue renvoyée
     * (Gerant ou Vendeur) dépend du rôle de l'appelant, déterminé depuis le
     * SecurityContext peuplé par JwtAuthenticationFilter. Le VENDEUR ne
     * verra donc JAMAIS coutAchat, quel que soit le contenu de la requête.
     *
     * Un VENDEUR ne voit par défaut que les articles actifs (actif=true) :
     * s'il ne précise pas le filtre "actif", on le force à true pour lui.
     * Le GERANT, lui, peut voir aussi les articles désactivés (actif=false
     * envoyé explicitement, ou aucun filtre = tout).
     */
    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @GetMapping
    public ResponseEntity<Page<?>> search(
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String terme,
            Pageable pageable,
            Authentication authentication
    ) {
        boolean isGerant = isGerant(authentication);

        Boolean actifEffectif = actif;
        if (!isGerant && actifEffectif == null) {
            actifEffectif = true;
        }

        Page<Article> articles = articleService.search(categorieId, actifEffectif, terme, pageable);

        Page<?> response = isGerant
                ? articles.map(ArticleGerantResponse::from)
                : articles.map(ArticleVendeurResponse::from);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id, Authentication authentication) {
        Article article = articleService.findEntityById(id);
        return ResponseEntity.ok(isGerant(authentication)
                ? ArticleGerantResponse.from(article)
                : ArticleVendeurResponse.from(article));
    }

    @PreAuthorize("hasRole('GERANT')")
    @PostMapping
    public ResponseEntity<ArticleGerantResponse> create(@Valid @RequestBody ArticleCreateRequest request) {
        Article created = articleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ArticleGerantResponse.from(created));
    }

    @PreAuthorize("hasRole('GERANT')")
    @PutMapping("/{id}")
    public ResponseEntity<ArticleGerantResponse> update(
            @PathVariable Long id, @Valid @RequestBody ArticleUpdateRequest request) {
        Article updated = articleService.update(id, request);
        return ResponseEntity.ok(ArticleGerantResponse.from(updated));
    }

    @PreAuthorize("hasRole('GERANT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isGerant(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_GERANT"));
    }
}