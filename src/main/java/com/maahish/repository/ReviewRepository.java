package com.maahish.repository;

import com.maahish.entity.Review;
import com.maahish.entity.Product;
import com.maahish.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product, Pageable pageable);

    boolean existsByProductAndUser(Product product, User user);
}
