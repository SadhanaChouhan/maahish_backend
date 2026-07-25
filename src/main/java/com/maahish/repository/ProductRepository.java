package com.maahish.repository;

import com.maahish.entity.Product;
import com.maahish.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    boolean existsBySlug(String slug);

    long countByStatus(ProductStatus status);

    long countBySellerId(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);

    Page<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    Page<Product> findByStatusAndLatestArrivalTrueOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

    Page<Product> findByStatusAndBestSellerTrueOrderByRatingDescCreatedAtDesc(ProductStatus status, Pageable pageable);
}
