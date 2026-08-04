package com.friperie.felana.catalog.repository;

import com.friperie.felana.catalog.domain.Article;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtres composables pour la recherche d'articles. Chaque méthode renvoie
 * un Specification qu'on combine avec .and() dans le service, uniquement
 * si le critère correspondant est fourni (sinon on ne l'ajoute pas).
 */
public class ArticleSpecifications {

    private ArticleSpecifications() {
    }

    public static Specification<Article> hasCategorie(Long categorieId) {
        return (root, query, cb) -> categorieId == null ? null
                : cb.equal(root.get("categorie").get("id"), categorieId);
    }

    public static Specification<Article> isActif(Boolean actif) {
        return (root, query, cb) -> actif == null ? null
                : cb.equal(root.get("actif"), actif);
    }

    public static Specification<Article> containsTerm(String terme) {
        return (root, query, cb) -> {
            if (terme == null || terme.isBlank()) {
                return null;
            }
            String pattern = "%" + terme.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nom")), pattern),
                    cb.like(cb.lower(root.get("reference")), pattern)
            );
        };
    }
}