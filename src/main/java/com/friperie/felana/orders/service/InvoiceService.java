package com.friperie.felana.orders.service;

import com.friperie.felana.common.config.MagasinProperties;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.domain.Commande;
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

        BigDecimal sousTotal = commande.getTotalAchat();
        BigDecimal tauxTaxe = magasinProperties.getTauxTaxe();
        BigDecimal montantTaxe = sousTotal
                .multiply(tauxTaxe)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = sousTotal.add(montantTaxe);

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
                commande.getVendeur().getUsername(),
                commande.getLignes().stream().map(InvoiceLigneResponse::from).toList(),
                sousTotal,
                tauxTaxe,
                montantTaxe,
                total,
                commande.getStatut()
        );
    }
}