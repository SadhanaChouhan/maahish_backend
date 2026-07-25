package com.maahish.repository;

import com.maahish.entity.Wishlist;
import com.maahish.entity.User;
import com.maahish.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);

    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
