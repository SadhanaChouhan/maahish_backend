package com.maahish.controller;

import com.maahish.dto.request.CartItemRequest;
import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.CartResponse;
import com.maahish.security.SecurityUtil;
import com.maahish.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(SecurityUtil.getCurrentUserId())));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addItem(SecurityUtil.getCurrentUserId(), request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateItem(SecurityUtil.getCurrentUserId(), itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeItem(SecurityUtil.getCurrentUserId(), itemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart(SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
