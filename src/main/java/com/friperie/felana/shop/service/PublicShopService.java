package com.friperie.felana.shop.service;

import com.friperie.felana.catalog.domain.Article;
import com.friperie.felana.catalog.service.ArticleService;
import com.friperie.felana.common.exception.ResourceNotFoundException;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.LigneCommande;
import com.friperie.felana.orders.domain.StatutCommande;
import com.friperie.felana.orders.repository.ClientRepository;
import com.friperie.felana.orders.repository.CommandeRepository;
import com.friperie.felana.shop.dto.ArticlePublicDTO;
import com.friperie.felana.shop.dto.request.PublicOrderItemRequest;
import com.friperie.felana.shop.dto.request.PublicOrderRequest;
import com.friperie.felana.shop.dto.response.PublicOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicShopService {

    private final ArticleService articleService;
    private final ClientRepository clientRepository;
    private final CommandeRepository commandeRepository;

    public Page<ArticlePublicDTO> findArticles(Pageable pageable) {
        // Un visiteur ne voit que les articles actifs ET explicitement publiés sur la
        // vitrine.
        Page<Article> page = articleService.search(null, true, null, pageable);
        List<ArticlePublicDTO> filtres = page.getContent().stream()
                .filter(Article::isPublieVitrine)
                .map(ArticlePublicDTO::from)
                .toList();
        return new PageImpl<>(filtres, pageable, filtres.size());
    }

    public ArticlePublicDTO findArticleById(Long id) {
        Article article = articleService.findEntityById(id);
        if (!article.isActif() || !article.isPublieVitrine()) {
            throw new ResourceNotFoundException("Article introuvable, id=" + id);
        }
        return ArticlePublicDTO.from(article);
    }

    /**
     * Création d'une commande "guest checkout" : trouve ou crée le client
     * par son numéro de téléphone, vérifie le stock de chaque ligne, la
     * décrémente, calcule le total, sans aucun vendeur associé.
     *
     * @Transactional garantit qu'en cas de rupture de stock sur une ligne,
     *                TOUT est annulé (y compris les décréments déjà faits sur les
     *                lignes
     *                précédentes) - pas de commande partiellement enregistrée.
     */
    @Transactional
    public PublicOrderResponse createOrder(PublicOrderRequest request, Client clientConnecte) {

        Commande commande = Commande.builder()
                .reference(genererReference())
                .statut(StatutCommande.EN_ATTENTE_VALIDATION)
                .client(clientConnecte)
                .vendeur(null) // pas de vendeur pour une commande en ligne
                .modePaiement(request.modePaiement().name())
                .totalAchat(BigDecimal.ZERO)
                .build();

        List<LigneCommande> lignes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PublicOrderItemRequest item : request.items()) {
            Article article = articleService.findEntityById(item.articleId());

            if (!article.isActif()) {
                throw new IllegalArgumentException(
                        "L'article '" + article.getNom() + "' n'est plus disponible.");
            }

            articleService.verifierDisponibilite(article.getId(), item.quantite());

            LigneCommande ligne = LigneCommande.builder()
                    .commande(commande)
                    .article(article)
                    .quantite(item.quantite())
                    .prixUnitaire(article.getPrixVente())
                    .build();

            lignes.add(ligne);
            total = total.add(ligne.getSousTotal());
        }

        commande.setLignes(lignes);
        commande.setTotalAchat(total);

        Commande saved = commandeRepository.save(commande);

        return new PublicOrderResponse(
                saved.getReference(),
                saved.getTotalAchat(),
                request.modePaiement().name(),
                buildInstructions(request.modePaiement()));
    }

    /* 
    private Client creerNouveauClient(PublicOrderRequest request) {
        Client client = Client.builder()
                .nom(request.nomClient())
                .telephone(request.telephone())
                .adresse(request.adresseLivraison())
                .build();
        return clientRepository.save(client);
    }*/

    private String genererReference() {
        String prefix = "SHOP-" + Year.now().getValue() + "-";
        long count = commandeRepository.countByReferenceStartingWith(prefix) + 1;
        return prefix + String.format("%06d", count);
    }

    private String buildInstructions(com.friperie.felana.shop.domain.ModePaiement mode) {
        return switch (mode) {
            case MVOLA_MANUEL ->
                "Effectuez votre transfert Mvola au 034 XX XXX XX, puis attendez la confirmation par téléphone.";
            case ORANGE_MONEY_MANUEL ->
                "Effectuez votre transfert Orange Money au 032 XX XXX XX, puis attendez la confirmation par téléphone.";
            case ESPECES -> "Le paiement en espèces se fera à la livraison ou au retrait.";
        };
    }
}