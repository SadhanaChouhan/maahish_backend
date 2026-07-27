package com.maahish.cart.service;

import com.maahish.catalog.dto.response.ProductSummaryResponse;


import java.util.List;

public interface WishlistService {

    List<ProductSummaryResponse> getWishlist(Long userId);

    void addToWishlist(Long userId, Long productId);

    void removeFromWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);
}
