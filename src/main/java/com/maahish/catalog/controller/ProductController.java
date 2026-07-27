package com.maahish.catalog.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.constants.AppConstants;
import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.service.ProductService;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.catalog.entity.Review;
import com.maahish.catalog.dto.request.ReviewRequest;
import com.maahish.common.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product listing, search, and details")
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @Operation(summary = "Search and filter products")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long fabricTypeId,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String occasion,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PageResponse<ProductSummaryResponse> result = productService.searchProducts(
                keyword, categoryId, fabricTypeId, color, occasion, minPrice, maxPrice, sortBy, sortDir, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by SEO slug")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping(value = "/reviews/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a photo for a product review")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadReviewPhoto(
            @RequestParam("file") MultipartFile file) {
        SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Review photo uploaded", cloudinaryService.uploadReviewPhoto(file)));
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Add product review")
    public ResponseEntity<ApiResponse<Void>> addReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        productService.addReview(id, SecurityUtil.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully"));
    }
}
