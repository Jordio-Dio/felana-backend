package com.friperie.felana.orders.repository;

import com.friperie.felana.orders.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;


public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    
    Optional<Client> findByTelephone(String telephone);
}   