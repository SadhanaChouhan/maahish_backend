package com.maahish.service;

import com.maahish.dto.request.ProductRequest;
import com.maahish.dto.request.ReviewRequest;
import com.maahish.dto.response.HomeResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.ProductDetailResponse;
import com.maahish.dto.response.ProductSummaryResponse;

import com.maahish.entity.Seller;
import com.maahish.enums.ProductStatus;

import java.math.BigDecimal;

public interface ProductService {

    HomeResponse getHomePage();

    PageResponse<ProductSummaryResponse> searchProducts(
            String keyword,
            Long categoryId,
            String fabric,
            String color,
            String occasion,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            int page,
            int size
    );

    ProductDetailResponse getProductBySlug(String slug);

    ProductDetailResponse getProductById(Long id);

    void addReview(Long productId, Long userId, ReviewRequest request);

    void adminRemoveProduct(Long id);

    PageResponse<ProductSummaryResponse> adminSearchProducts(
            String keyword, Long categoryId, ProductStatus status, String sortBy, String sortDir, int page, int size);

    ProductDetailResponse createProductForSeller(Seller seller, ProductRequest request);

    ProductDetailResponse updateProductForSeller(Seller seller, Long productId, ProductRequest request);

    void deleteProductForSeller(Seller seller, Long productId);
}
