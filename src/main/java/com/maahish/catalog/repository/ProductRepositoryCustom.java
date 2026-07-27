package com.maahish.catalog.repository;

import com.maahish.catalog.entity.Product;
import com.maahish.catalog.enums.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepositoryCustom {

    Page<Product> searchProducts(
            String keyword,
            Long categoryId,
            Long fabricTypeId,
            String color,
            String occasion,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            Pageable pageable
    );

    Page<Product> adminSearchProducts(
            String keyword,
            Long categoryId,
            ProductStatus status,
            String sortBy,
            String sortDir,
            Pageable pageable
    );

    List<Product> findRelatedProducts(Long productId, Long categoryId, int limit);
}
