package com.friperie.felana.catalog.repository;

import com.friperie.felana.catalog.domain.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    boolean existsByNom(String nom);
}