package com.maahish.catalog.repository;

import com.maahish.catalog.entity.Product;
import com.maahish.catalog.enums.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

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
