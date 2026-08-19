package com.friperie.felana.shop.dto.response;

import java.math.BigDecimal;

/**
 * Réponse renvoyée après création d'une commande en ligne. Contient tout
 * ce qu'il faut pour afficher l'écran de confirmation avec les
 * instructions de paiement manuel.
 */
public record PublicOrderResponse(
        String reference,
        BigDecimal totalAchat,
        String modePaiement,
        String instructionsPaiement
) {
}