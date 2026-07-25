package com.maahish.service;

import com.maahish.dto.response.ProductSummaryResponse;

import java.util.List;

public interface WishlistService {

    List<ProductSummaryResponse> getWishlist(Long userId);

    void addToWishlist(Long userId, Long productId);

    void removeFromWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);
}
