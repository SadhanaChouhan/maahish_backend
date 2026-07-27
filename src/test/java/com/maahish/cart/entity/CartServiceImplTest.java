package com.maahish.cart.entity;

import com.maahish.cart.repository.CartItemRepository;
import com.maahish.cart.dto.request.CartItemRequest;
import com.maahish.cart.repository.CartRepository;
import com.maahish.cart.service.CartServiceImpl;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ShoppingAccessValidator shoppingAccessValidator;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addItem_whenProductAlreadyInCart_doesNotIncreaseQuantity() {
        Product product = Product.builder()
                .id(1L)
                .name("Silk Saree")
                .slug("silk-saree")
                .status(ProductStatus.ACTIVE)
                .stock(10)
                .sellingPrice(BigDecimal.TEN)
                .build();
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        CartItem existing = CartItem.builder().cart(cart).product(product).quantity(2).build();
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existing));
        when(productMapper.toSummary(product)).thenReturn(ProductSummaryResponse.builder().primaryImageUrl("img").build());

        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        var response = cartService.addItem(1L, request);

        assertEquals(2, response.getTotalItems());
        verify(cartRepository, never()).save(any());
    }
}
