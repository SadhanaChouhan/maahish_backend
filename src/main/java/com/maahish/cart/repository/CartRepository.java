package com.maahish.cart.repository;

import com.maahish.cart.entity.Cart;
import com.maahish.user.entity.User;

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
