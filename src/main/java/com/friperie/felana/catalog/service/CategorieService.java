package com.friperie.felana.catalog.service;

import com.friperie.felana.catalog.domain.Categorie;
import com.friperie.felana.catalog.dto.request.CategorieRequest;
import com.friperie.felana.catalog.dto.response.CategorieResponse;
import com.friperie.felana.catalog.repository.CategorieRepository;
import com.friperie.felana.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public List<CategorieResponse> findAll() {
        return categorieRepository.findAll().stream()
                .map(CategorieResponse::from)
                .toList();
    }

    @Transactional
    public CategorieResponse create(CategorieRequest request) {
        if (categorieRepository.existsByNom(request.nom())) {
            throw new DataIntegrityViolationException("Une catégorie avec ce nom existe déjà.");
        }
        Categorie categorie = Categorie.builder()
                .nom(request.nom())
                .description(request.description())
                .build();
        return CategorieResponse.from(categorieRepository.save(categorie));
    }

    @Transactional
    public CategorieResponse update(Long id, CategorieRequest request) {
        Categorie categorie = findEntityById(id);
        categorie.setNom(request.nom());
        categorie.setDescription(request.description());
        return CategorieResponse.from(categorieRepository.save(categorie));
    }

    @Transactional
    public void delete(Long id) {
        Categorie categorie = findEntityById(id);
        categorieRepository.delete(categorie);
    }

    public Categorie findEntityById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable, id=" + id));
    }
}