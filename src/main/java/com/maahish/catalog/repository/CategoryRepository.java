package com.maahish.catalog.repository;

import com.maahish.catalog.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByActiveTrueOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}
