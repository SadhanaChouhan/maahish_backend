package com.maahish.catalog.entity;

import com.maahish.common.exception.BadRequestException;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.catalog.dto.request.ReviewRequest;
import com.maahish.catalog.entity.Category;
import com.maahish.catalog.repository.CategoryRepository;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.repository.ReviewRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.service.CategoryService;
import com.maahish.catalog.service.FabricTypeService;
import com.maahish.catalog.service.ProductServiceImpl;
import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.order.repository.OrderItemRepository;
import com.maahish.seller.mapper.SellerMapper;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplReviewTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FabricTypeRepository fabricTypeRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductMapper productMapper;
    @Mock private CategoryService categoryService;
    @Mock private FabricTypeService fabricTypeService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private SellerMapper sellerMapper;
    @Mock private ShoppingAccessValidator shoppingAccessValidator;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void addReview_withoutDeliveredPurchase_throws() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).build();
        User user = User.builder().id(10L).role(UserRole.ROLE_USER).build();
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Lovely");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(reviewRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(orderItemRepository.existsDeliveredPurchaseByUserAndProduct(10L, 1L)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> productService.addReview(1L, 10L, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_nonCustomerRole_throwsBeforeLookup() {
        doThrow(new ForbiddenException("denied")).when(shoppingAccessValidator).requireCustomer(10L);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);

        assertThrows(ForbiddenException.class,
                () -> productService.addReview(1L, 10L, request));
        verify(productRepository, never()).findById(any());
    }

    @Test
    void addReview_verifiedPurchase_savesReview() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE)
                .stock(5).rating(BigDecimal.ZERO).reviewCount(0).build();
        User user = User.builder().id(10L).role(UserRole.ROLE_USER).build();
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Beautiful weave");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(reviewRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(orderItemRepository.existsDeliveredPurchaseByUserAndProduct(10L, 1L)).thenReturn(true);
        when(reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(
                        Review.builder().rating(5).approved(true).build())));

        productService.addReview(1L, 10L, request);

        verify(reviewRepository).save(any(Review.class));
        verify(productRepository).save(product);
    }
}
