package com.maahish.service;

import com.maahish.dto.request.CartItemRequest;
import com.maahish.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateItem(Long userId, Long itemId, CartItemRequest request);

    CartResponse removeItem(Long userId, Long itemId);

    void clearCart(Long userId);
}
