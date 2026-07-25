package com.maahish.repository;

import com.maahish.entity.Seller;
import com.maahish.enums.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);

    Optional<Seller> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    Optional<Seller> findByPlatformOwnedTrue();

    long countByStatus(SellerStatus status);

    @Query("""
            SELECT s FROM Seller s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:keyword IS NULL OR :keyword = '' OR
                   LOWER(s.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(s.ownerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Seller> searchSellers(@Param("status") SellerStatus status,
                               @Param("keyword") String keyword,
                               Pageable pageable);
}
