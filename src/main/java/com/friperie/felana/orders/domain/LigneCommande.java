package com.friperie.felana.orders.domain;

import com.friperie.felana.catalog.domain.Article;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private Integer quantite;

    /**
     * Snapshot du prix de vente de l'article AU MOMENT de la commande.
     * Volontairement dupliqué depuis Article.prixVente : si le GERANT
     * change le prix d'un article plus tard, l'historique des ventes
     * passées ne doit pas être faussé rétroactivement.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Transient
    public BigDecimal getSousTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}