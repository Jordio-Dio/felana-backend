package com.friperie.felana.orders.repository;

import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.StatutCommande;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class CommandeSpecifications {

    private CommandeSpecifications() {
    }

    public static Specification<Commande> dateApres(Instant dateDebut) {
        return (root, query, cb) -> dateDebut == null ? null
                : cb.greaterThanOrEqualTo(root.get("dateCommande"), dateDebut);
    }

    public static Specification<Commande> dateAvant(Instant dateFin) {
        return (root, query, cb) -> dateFin == null ? null
                : cb.lessThanOrEqualTo(root.get("dateCommande"), dateFin);
    }

    public static Specification<Commande> hasStatut(StatutCommande statut) {
        return (root, query, cb) -> statut == null ? null
                : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Commande> hasClient(Long clientId) {
        return (root, query, cb) -> clientId == null ? null
                : cb.equal(root.get("client").get("id"), clientId);
    }

    public static Specification<Commande> hasVendeur(Long vendeurId) {
        return (root, query, cb) -> vendeurId == null ? null
                : cb.equal(root.get("vendeur").get("id"), vendeurId);
    }
}