package com.friperie.felana.orders.service;

import com.friperie.felana.common.config.MagasinProperties;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.LigneCommande;
import com.friperie.felana.orders.dto.response.InvoiceLigneResponse;
import com.friperie.felana.orders.dto.response.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Construit la vue "reçu/facture" à partir d'une commande existante.
 * Ne modifie AUCUNE donnée : c'est un service de lecture/présentation pur,
 * la commande doit déjà exister et être complète (créée via CommandeService).
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

        private final MagasinProperties magasinProperties;

        public InvoiceResponse generate(Commande commande) {
                Client client = commande.getClient();

                BigDecimal sousTotalLignes = commande.getLignes().stream().map(LigneCommande::getSousTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal tauxTaxe = magasinProperties.getTauxTaxe();
                BigDecimal montantTaxe = commande.getTotalAchat()
                                .multiply(tauxTaxe)
                                .setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = commande.getTotalAchat().add(montantTaxe);

                String clientNomComplet = (client.getPrenom() != null ? client.getPrenom() + " " : "")
                                + client.getNom();

                return new InvoiceResponse(
                                commande.getReference(),
                                commande.getDateCommande(),
                                magasinProperties.getNom(),
                                magasinProperties.getAdresse(),
                                magasinProperties.getTelephone(),
                                clientNomComplet,
                                client.getTelephone(),
                                client.getEmail(),
                                commande.getVendeur().getName(),
                                commande.getLignes().stream().map(InvoiceLigneResponse::from).toList(),
                                sousTotalLignes,
                                commande.getRemise()
                                tauxTaxe,
                                montantTaxe,
                                total,
                                commande.getStatut());
        }
}