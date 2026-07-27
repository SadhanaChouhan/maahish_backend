package com.maahish.catalog.repository;

import com.maahish.catalog.entity.FabricType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FabricTypeRepository extends JpaRepository<FabricType, Long> {

    List<FabricType> findByActiveTrueOrderByNameAsc();

    Optional<FabricType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);
}
