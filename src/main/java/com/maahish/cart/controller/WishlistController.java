package com.maahish.cart.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.common.security.SecurityUtil;
import com.maahish.cart.entity.Wishlist;
import com.maahish.cart.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Wishlist", description = "Wishlist management")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getWishlist() {
        return ResponseEntity.ok(ApiResponse.success(
                wishlistService.getWishlist(SecurityUtil.getCurrentUserId())));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<Void>> add(@PathVariable Long productId) {
        wishlistService.addToWishlist(SecurityUtil.getCurrentUserId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(SecurityUtil.getCurrentUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist"));
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Check if product is in wishlist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> check(@PathVariable Long productId) {
        boolean inWishlist = wishlistService.isInWishlist(SecurityUtil.getCurrentUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWishlist", inWishlist)));
    }
}
