package com.friperie.felana.orders.dto.request;

import com.friperie.felana.orders.domain.StatutCommande;

import java.time.Instant;

/**
 * Tous les champs sont optionnels (nullable) : chaque filtre n'est appliqué
 * QUE s'il est fourni. Consommé via @ModelAttribute (paramètres de query
 * string classiques, ex: ?statut=PAYEE&dateDebut=2026-01-01T00:00:00Z).
 */
public record OrderHistoryFilterRequest(
        Instant dateDebut,
        Instant dateFin,
        StatutCommande statut,
        Long clientId,
        Long vendeurId
) {
}