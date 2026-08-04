package com.friperie.felana.catalog.repository;

import com.friperie.felana.catalog.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    boolean existsByReference(String reference);
}