package com.friperie.felana.orders.domain;

import com.friperie.felana.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence lisible pour le client/reçu, ex: "CMD-2026-000042". Générée en
     * service.
     */
    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false, updatable = false)
    private Instant dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Le vendeur qui a enregistré la vente (traçabilité). Utile pour les
     * statistiques de vente par vendeur plus tard.
     * Mais null pour une commande passée en ligne par un client(e) anonyme via le
     * module shop
     * - il n'y pas de vendeur associé(ou impliqué) à la commande,
     * c'est le client qui l'a passée lui-même.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = true)
    private User vendeur;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAchat;

    /**
     * Remise ponctuelle appliquée à cette commande, en montant fixe (pas en
     * pourcentage - plus simple et sans ambiguïté pour la gérante). Optionnelle,
     * saisie manuellement à la création. Toujours positive ou nulle - jamais
     * négative (une remise ne peut pas AUGMENTER le total).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;

    /**
     * Mode de paiment choisi pour les commandes en ligne (guest checkout)
     * Null pour les ventes internes classiques
     */
    private String modePaiement;
    
    /**
     * cascade = ALL : sauvegarder/supprimer une Commande sauvegarde/supprime
     * ses lignes automatiquement. orphanRemoval = true : si une ligne est
     * retirée de cette liste en mémoire, elle est supprimée en base (pas
     * utilisé dans cette version puisqu'on ne permet pas l'édition des
     * lignes après création, mais c'est la bonne pratique par défaut).
     */
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneCommande> lignes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCommande = Instant.now();
    }
}