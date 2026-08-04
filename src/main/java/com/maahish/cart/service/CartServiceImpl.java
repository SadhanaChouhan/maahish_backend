package com.maahish.cart.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.cart.entity.Cart;
import com.maahish.cart.entity.CartItem;
import com.maahish.cart.repository.CartItemRepository;
import com.maahish.cart.dto.request.CartItemRequest;
import com.maahish.cart.dto.response.CartItemResponse;
import com.maahish.cart.repository.CartRepository;
import com.maahish.cart.dto.response.CartResponse;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final ShoppingAccessValidator shoppingAccessValidator;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        shoppingAccessValidator.requireCustomer(userId);
        Cart cart = getOrCreateCart(userId);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        Cart cart = getOrCreateCart(userId);
        Product product = getActiveProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);
        if (item != null) {
            return toCartResponse(cart);
        }
        item = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .build();
        cart.getItems().add(item);
        cartRepository.save(cart);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, CartItemRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        Product product = getActiveProduct(request.getProductId());
        validateStock(product, request.getQuantity());
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        shoppingAccessValidator.requireCustomer(userId);
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        shoppingAccessValidator.requireCustomer(userId);
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    Cart cart = Cart.builder().user(user).build();
                    return cartRepository.save(cart);
                });
    }

    private Product getActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available");
        }
        return product;
    }

    private void validateStock(Product product, int quantity) {
        int available = ProductStockService.availableStock(product);
        if (available < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + available);
        }
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        String imageUrl = productMapper.toSummary(product).getPrimaryImageUrl();
        BigDecimal unitPrice = product.getSellingPrice();
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .imageUrl(imageUrl)
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .availableStock(ProductStockService.availableStock(product))
                .build();
    }
}
