package com.friperie.felana.catalog.service;

import com.friperie.felana.catalog.domain.Article;
import com.friperie.felana.catalog.domain.Categorie;
import com.friperie.felana.catalog.dto.request.ArticleCreateRequest;
import com.friperie.felana.catalog.dto.request.ArticleUpdateRequest;
import com.friperie.felana.catalog.repository.ArticleRepository;
import com.friperie.felana.catalog.repository.ArticleSpecifications;
import com.friperie.felana.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategorieService categorieService;

    /**
     * Retourne des entités Article brutes ; c'est le CONTROLLER qui choisit
     * le DTO de sortie (Gerant ou Vendeur) selon le rôle de l'appelant. On
     * garde le service agnostique du rôle : sa responsabilité est l'accès
     * aux données, pas la présentation.
     */
    public Page<Article> search(Long categorieId, Boolean actif, String terme, Pageable pageable) {
        Specification<Article> spec = Specification
                .where(ArticleSpecifications.hasCategorie(categorieId))
                .and(ArticleSpecifications.isActif(actif))
                .and(ArticleSpecifications.containsTerm(terme));

        return articleRepository.findAll(spec, pageable);
    }

    public Article findEntityById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable, id=" + id));
    }

    @Transactional
    public Article create(ArticleCreateRequest request) {
        if (request.reference() != null && articleRepository.existsByReference(request.reference())) {
            throw new DataIntegrityViolationException("Un article avec cette référence existe déjà.");
        }
        Categorie categorie = categorieService.findEntityById(request.categorieId());

        Article article = Article.builder()
                .reference(request.reference())
                .nom(request.nom())
                .description(request.description())
                .prixVente(request.prixVente())
                .coutMatiere(request.coutMatiere())
                .coutAccessoire(request.coutAccessoire())
                .coutMainOeuvre(request.coutMainOeuvre())
                .coutAchat(calculerCoutAchat(request.coutMatiere(), request.coutAccessoire(), request.coutMainOeuvre()))
                .quantiteStock(request.quantiteStock())
                .seuilAlerte(request.seuilAlerte() != null ? request.seuilAlerte() : 3)
                .imageUrl(request.imageUrl())
                .actif(true)
                .categorie(categorie)
                .build();

        return articleRepository.save(article);
    }

    @Transactional
    public Article update(Long id, ArticleUpdateRequest request) {
        Article article = findEntityById(id);
        Categorie categorie = categorieService.findEntityById(request.categorieId());

        article.setNom(request.nom());
        article.setDescription(request.description());
        article.setPrixVente(request.prixVente());
        article.setCoutMatiere(request.coutMatiere());
        article.setCoutAccessoire(request.coutAccessoire());
        article.setCoutMainOeuvre(request.coutMainOeuvre());
        article.setCoutAchat(
                calculerCoutAchat(request.coutMatiere(), request.coutAccessoire(), request.coutMainOeuvre()));
        article.setQuantiteStock(request.quantiteStock());
        if (request.seuilAlerte() != null) {
            article.setSeuilAlerte(request.seuilAlerte());
        }
        article.setImageUrl(request.imageUrl());
        if (request.actif() != null) {
            article.setActif(request.actif());
        }
        article.setCategorie(categorie);

        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        Article article = findEntityById(id);
        articleRepository.delete(article);
    }

    /**
     * Utilisé plus tard par le module Commandes pour décrémenter le stock à la
     * vente.
     */
    @Transactional
    public void decrementerStock(Long articleId, int quantite) {
        Article article = findEntityById(articleId);
        if (article.getQuantiteStock() < quantite) {
            throw new IllegalArgumentException(
                    "Stock insuffisant pour l'article '" + article.getNom() + "'.");
        }
        article.setQuantiteStock(article.getQuantiteStock() - quantite);
        articleRepository.save(article);
    }

    /** Utilisé lors de l'annulation d'une commande : remet la quantité au stock. */
    @Transactional
    public void restaurerStock(Long articleId, int quantite) {
        Article article = findEntityById(articleId);
        article.setQuantiteStock(article.getQuantiteStock() + quantite);
        articleRepository.save(article);
    }

    /**
     * Calcule et applique le coût de revient total à partir des 3 sous-coûts.
     * Appelée systématiquement à la création ET à la modification, pour que
     * coutAchat reste TOUJOURS cohérent avec ses composants - jamais désynchronisé.
     */
    private BigDecimal calculerCoutAchat(BigDecimal coutMatiere, BigDecimal coutAccessoire, BigDecimal coutMainOeuvre) {
        return coutMatiere.add(coutAccessoire).add(coutMainOeuvre);
    }
}