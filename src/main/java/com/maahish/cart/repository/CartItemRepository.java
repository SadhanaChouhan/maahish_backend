package com.maahish.cart.repository;

import com.maahish.cart.entity.Cart;
import com.maahish.cart.entity.CartItem;
import com.maahish.catalog.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);
}
