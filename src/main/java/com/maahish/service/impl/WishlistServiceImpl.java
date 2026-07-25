package com.maahish.service.impl;

import com.maahish.dto.response.ProductSummaryResponse;
import com.maahish.entity.Product;
import com.maahish.entity.User;
import com.maahish.entity.Wishlist;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.mapper.ProductMapper;
import com.maahish.repository.ProductRepository;
import com.maahish.repository.UserRepository;
import com.maahish.repository.WishlistRepository;
import com.maahish.security.ShoppingAccessValidator;
import com.maahish.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShoppingAccessValidator shoppingAccessValidator;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getWishlist(Long userId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(Wishlist::getProduct)
                .map(productMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new BadRequestException("Product already in wishlist");
        }

        wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return wishlistRepository.existsByUserAndProduct(user, product);
    }
}
