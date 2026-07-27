package com.maahish.cart.service;

import com.maahish.cart.dto.request.CartItemRequest;
import com.maahish.cart.dto.response.CartResponse;


public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateItem(Long userId, Long itemId, CartItemRequest request);

    CartResponse removeItem(Long userId, Long itemId);

    void clearCart(Long userId);
}
