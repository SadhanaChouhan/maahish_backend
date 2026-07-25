package com.maahish.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepositoryCustom {

    Page<com.maahish.entity.Product> searchProducts(
            String keyword,
            Long categoryId,
            String fabric,
            String color,
            String occasion,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            Pageable pageable
    );

    Page<com.maahish.entity.Product> adminSearchProducts(
            String keyword,
            Long categoryId,
            com.maahish.enums.ProductStatus status,
            String sortBy,
            String sortDir,
            Pageable pageable
    );

    List<com.maahish.entity.Product> findRelatedProducts(Long productId, Long categoryId, int limit);
}
