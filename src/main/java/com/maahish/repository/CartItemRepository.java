package com.maahish.repository;

import com.maahish.entity.CartItem;
import com.maahish.entity.Cart;
import com.maahish.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);
}
