package com.friperie.felana.orders.repository;

import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.domain.Commande;
import com.friperie.felana.orders.domain.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommandeRepository extends JpaRepository<Commande, Long> , JpaSpecificationExecutor<Commande>{

    Page<Commande> findByClient(Client client, Pageable pageable);

    Page<Commande> findByStatut(StatutCommande statut, Pageable pageable);

    long countByReferenceStartingWith(String prefix);

    long countByStatut(StatutCommande statut);
}
