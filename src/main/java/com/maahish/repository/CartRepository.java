package com.maahish.repository;

import com.maahish.entity.Cart;
import com.maahish.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.seller"})
    Optional<Cart> findByUserId(Long userId);
}
