package com.friperie.felana.orders.service;

import com.friperie.felana.auth.domain.User;
import com.friperie.felana.catalog.domain.Article;
import com.friperie.felana.catalog.service.ArticleService;
import com.friperie.felana.common.exception.ResourceNotFoundException;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.LigneCommande;
import com.friperie.felana.orders.domain.StatutCommande;
import com.friperie.felana.orders.dto.request.CommandeCreateRequest;
import com.friperie.felana.orders.dto.request.LigneCommandeRequest;
import com.friperie.felana.orders.dto.request.OrderHistoryFilterRequest;
import com.friperie.felana.orders.repository.CommandeRepository;
import com.friperie.felana.orders.repository.CommandeSpecifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientService clientService;
    private final ArticleService articleService;

    public Page<Commande> findAll(Pageable pageable) {
        return commandeRepository.findAll(pageable);
    }

    public Page<Commande> findByClient(Long clientId, Pageable pageable) {
        Client client = clientService.findEntityById(clientId);
        return commandeRepository.findByClient(client, pageable);
    }

    public Commande findEntityById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable, id=" + id));
    }

    /**
     * Création d'une commande complète : vérifie le client, construit chaque
     * ligne à partir du prix courant de l'article (figé dans la ligne),
     * décrémente le stock, calcule le total, génère une référence lisible.
     *
     * @Transactional est CRITIQUE ici : si le décrément de stock d'une ligne
     *                échoue (stock insuffisant), TOUT doit être annulé - y compris
     *                les
     *                décréments déjà faits sur les lignes précédentes de la même
     *                commande.
     *                Sans cette annotation, on risquerait un stock incohérent en
     *                cas
     *                d'erreur en cours de traitement.
     */
    @Transactional
    public Commande create(CommandeCreateRequest request, User vendeurConnecte) {
        Client client = clientService.findEntityById(request.clientId());

        Commande commande = Commande.builder()
                .reference(genererReference())
                .statut(StatutCommande.EN_ATTENTE)
                .client(client)
                .vendeur(vendeurConnecte)
                .totalAchat(BigDecimal.ZERO)
                .build();

        List<LigneCommande> lignes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (LigneCommandeRequest ligneReq : request.lignes()) {
            Article article = articleService.findEntityById(ligneReq.articleId());

            // Décrémente le stock immédiatement ; lève une exception si insuffisant,
            // ce qui annule automatiquement toute la transaction (rollback).
            articleService.decrementerStock(article.getId(), ligneReq.quantite());

            LigneCommande ligne = LigneCommande.builder()
                    .commande(commande)
                    .article(article)
                    .quantite(ligneReq.quantite())
                    .prixUnitaire(article.getPrixVente())
                    .build();

            lignes.add(ligne);
            total = total.add(ligne.getSousTotal());
        }

        BigDecimal remiseAppliquee = request.remise() != null ? request.remise() : BigDecimal.ZERO;

        // Garde-fou : la remise ne peut jamais dépasser le total, sinon le total
        // deviendrait négatif, ce qui n'a aucun sens commercial.
        if (remiseAppliquee.compareTo(total) > 0) {
            throw new IllegalArgumentException(
                    "La remise (" + remiseAppliquee + ") ne peut pas dépasser le total de la commande (" + total
                            + ").");
        }

        commande.setLignes(lignes);
        commande.setRemise(remiseAppliquee);
        commande.setTotalAchat(total.subtract(remiseAppliquee));

        return commandeRepository.save(commande);
    }

    /**
     * Changement de statut. Règle métier : si on passe à ANNULEE depuis un
     * état non-annulé, on restitue le stock de chaque ligne (la vente
     * n'a finalement pas eu lieu).
     */
    @Transactional
    public Commande updateStatut(Long id, StatutCommande nouveauStatut) {
        Commande commande = findEntityById(id);

        boolean devientAnnulee = nouveauStatut == StatutCommande.ANNULEE
                && commande.getStatut() != StatutCommande.ANNULEE;

        if (devientAnnulee) {
            for (LigneCommande ligne : commande.getLignes()) {
                articleService.restaurerStock(ligne.getArticle().getId(), ligne.getQuantite());
            }
        }

        commande.setStatut(nouveauStatut);
        return commandeRepository.save(commande);
    }

    /** Référence lisible du type "CMD-2026-000001", incrémentée par année. */
    private String genererReference() {
        String prefix = "CMD-" + Year.now().getValue() + "-";
        long count = commandeRepository.countByReferenceStartingWith(prefix) + 1;
        return prefix + String.format("%06d", count);
    }

    /**
     * Recherche paginée avec filtres. Le paramètre vendeurIdForce permet au
     * controller d'imposer le filtre vendeur pour un VENDEUR (il ne voit que
     * SES ventes), en écrasant toute valeur qu'il aurait pu envoyer dans la
     * query string pour tenter de voir les ventes d'un collègue.
     */
    public Page<Commande> search(OrderHistoryFilterRequest filter, Long vendeurIdForce, Pageable pageable) {
        Long vendeurEffectif = vendeurIdForce != null ? vendeurIdForce : filter.vendeurId();

        Specification<Commande> spec = Specification
                .where(CommandeSpecifications.dateApres(filter.dateDebut()))
                .and(CommandeSpecifications.dateAvant(filter.dateFin()))
                .and(CommandeSpecifications.hasStatut(filter.statut()))
                .and(CommandeSpecifications.hasClient(filter.clientId()))
                .and(CommandeSpecifications.hasVendeur(vendeurEffectif));

        return commandeRepository.findAll(spec, pageable);
    }

    public long countAttenteValidation() {
        return commandeRepository.countByStatut(StatutCommande.EN_ATTENTE_VALIDATION);
    }

}
