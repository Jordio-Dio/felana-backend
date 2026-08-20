package com.friperie.felana.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Un article du catalogue (vêtement, accessoire artisanal...).
 *
 * ATTENTION sécurité : coutAchat ne doit JAMAIS transiter dans une réponse
 * destinée à un VENDEUR. Cette règle est appliquée au niveau du mapping
 * DTO (ArticleGerantResponse vs ArticleVendeurResponse), jamais ici -
 * l'entité elle-même reste complète, c'est le rôle du service/mapper de
 * filtrer avant de sérialiser.
 */
@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code unique optionnel (SKU), pratique pour identifier une pièce physique. */
    @Column(unique = true)
    private String reference;

    @Column(nullable = false)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixVente;

    /** Coût des matières premières utilisées pour cet article. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutMatiere = BigDecimal.ZERO;

    /** Coût des accessoires (boutons, fermetures, etc.). */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutAccessoire = BigDecimal.ZERO;

    /**
     * Coût de la main d'œuvre, estimé directement en montant par la gérante
     * (pas de taux horaire fixe - elle a confirmé l'estimer au cas par cas).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutMainOeuvre = BigDecimal.ZERO;

    /**
     * Coût de revient total, TOUJOURS dérivé des 3 champs ci-dessus.
     * Recalculé automatiquement à chaque création/modification (voir
     * ArticleService) - jamais saisi directement, pour garantir la cohérence.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal coutAchat;

    /**
     * Pourcentage de bénéfice souhaité pour cet article (ex: 0.50 = 50%).
     * Optionnel : la gérante a confirmé que ce taux varie selon l'article,
     * pas de valeur fixe globale. Sert uniquement à calculer une suggestion
     * de prix de vente - prixVente reste toujours modifiable manuellement.
     */
    @Column(precision = 5, scale = 4)
    private BigDecimal pourcentageMarge;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantiteStock = 0;

    /**
     * En dessous de ce seuil, l'article apparaît dans les alertes de stock bas
     * (GERANT).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer seuilAlerte = 3;

    /**
     * Plusieurs photos par article. L'ordre de la liste détermine l'ordre
     * d'affichage - la première image sert de photo de couverture. Stockées
     * comme URLs Cloudinary (upload géré côté frontend, le backend ne
     * reçoit et ne persiste que les URLs résultantes).
     */
    @ElementCollection
    @CollectionTable(name = "article_images", joinColumns = @JoinColumn(name = "article_id"))
    @OrderColumn(name = "position")
    @Column(name = "url", nullable = false)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Contrôle la visibilité sur la vitrine PUBLIQUE, indépendamment de
     * "actif" (qui contrôle la visibilité interne GERANT/VENDEUR). Un article
     * peut être actif en interne mais pas encore publié publiquement (ex: en
     * cours de préparation de fiche), ou l'inverse.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean publieVitrine = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Marge en valeur absolue. Ne doit être calculée/exposée que côté GERANT. */
    @Transient
    public BigDecimal getMarge() {
        return prixVente.subtract(coutAchat);
    }

    @Transient
    public boolean isStockBas() {
        return quantiteStock <= seuilAlerte;
    }

    /**
     * Prix de vente suggéré = coutAchat × (1 + pourcentageMarge).
     * Retourne null si aucun pourcentage n'est défini - dans ce cas, le
     * frontend n'affiche simplement pas de suggestion.
     */
    @Transient
    public BigDecimal getPrixVenteSuggere() {
        if (pourcentageMarge == null) {
            return null;
        }
        return coutAchat.multiply(BigDecimal.ONE.add(pourcentageMarge));
    }
}