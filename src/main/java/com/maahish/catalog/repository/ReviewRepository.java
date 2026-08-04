package com.maahish.catalog.repository;

import com.maahish.catalog.entity.Product;
import com.maahish.catalog.entity.Review;
import com.maahish.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product, Pageable pageable);

    boolean existsByProductAndUser(Product product, User user);

    void deleteByUser(User user);
}
