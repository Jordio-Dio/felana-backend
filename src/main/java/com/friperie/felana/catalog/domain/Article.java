package com.friperie.felana.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

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

    private String imageUrl;

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
}