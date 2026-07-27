package com.maahish.seller.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.constants.AppConstants;
import com.maahish.settlement.dto.response.CommissionRuleResponse;
import com.maahish.settlement.service.CommissionRuleService;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.order.entity.Order;
import com.maahish.order.enums.OrderStatus;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.dto.request.ProductRequest;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.common.security.SecurityUtil;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.dto.response.SellerDashboardResponse;
import com.maahish.seller.dto.response.SellerOrderResponse;
import com.maahish.seller.dto.request.SellerProductPricingRequest;
import com.maahish.seller.dto.request.SellerProductStockRequest;
import com.maahish.seller.dto.request.SellerProfileUpdateRequest;
import com.maahish.seller.dto.response.SellerResponse;
import com.maahish.seller.service.SellerService;
import com.maahish.seller.dto.response.SellerSettlementResponse;
import com.maahish.settlement.service.SettlementService;
import com.maahish.settlement.enums.SettlementStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller", description = "Seller dashboard, products, orders, and profile")
public class SellerController {

    private final SellerService sellerService;
    private final SettlementService settlementService;
    private final CommissionRuleService commissionRuleService;

    @GetMapping("/dashboard")
    @Operation(summary = "Seller dashboard statistics")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getDashboard(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get seller profile")
    public ResponseEntity<ApiResponse<SellerResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getProfile(SecurityUtil.getCurrentUserId())));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update seller profile")
    public ResponseEntity<ApiResponse<SellerResponse>> updateProfile(
            @Valid @RequestPart("data") SellerProfileUpdateRequest request,
            @RequestPart(value = "businessLogo", required = false) MultipartFile businessLogo,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated",
                sellerService.updateProfile(SecurityUtil.getCurrentUserId(), request, businessLogo, profileImage)));
    }

    @GetMapping("/products")
    @Operation(summary = "List seller products")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> listProducts(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getProducts(SecurityUtil.getCurrentUserId(), page, size)));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get seller product details")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getProduct(SecurityUtil.getCurrentUserId(), id)));
    }

    @PostMapping("/products")
    @Operation(summary = "Create a product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created",
                        sellerService.createProduct(SecurityUtil.getCurrentUserId(), request)));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update seller product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated",
                sellerService.updateProduct(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete seller product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        sellerService.deleteProduct(SecurityUtil.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Product discontinued"));
    }

    @PatchMapping("/products/{id}/stock")
    @Operation(summary = "Update product stock")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody SellerProductStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated",
                sellerService.updateStock(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @PatchMapping("/products/{id}/pricing")
    @Operation(summary = "Update product price and discount")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updatePricing(
            @PathVariable Long id,
            @Valid @RequestBody SellerProductPricingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pricing updated",
                sellerService.updatePricing(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a product image to Cloudinary")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProductImage(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Image uploaded", sellerService.uploadProductImage(SecurityUtil.getCurrentUserId(), file)));
    }

    @GetMapping("/orders")
    @Operation(summary = "List orders containing seller products")
    public ResponseEntity<ApiResponse<PageResponse<SellerOrderResponse>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getOrders(SecurityUtil.getCurrentUserId(), status, page, size)));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get seller order details")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getOrder(SecurityUtil.getCurrentUserId(), id)));
    }

    @PatchMapping("/orders/{id}/confirm")
    @Operation(summary = "Confirm order items for this seller")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(@PathVariable Long id) {
        sellerService.confirmOrder(SecurityUtil.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed"));
    }

    @GetMapping("/commission-rules")
    @Operation(summary = "List enabled commission rules for pricing guidance")
    public ResponseEntity<ApiResponse<java.util.List<CommissionRuleResponse>>> getCommissionRules() {
        return ResponseEntity.ok(ApiResponse.success(commissionRuleService.getEnabledRules()));
    }

    @GetMapping("/settlements")
    @Operation(summary = "List seller settlements")
    public ResponseEntity<ApiResponse<PageResponse<SellerSettlementResponse>>> getSettlements(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementService.sellerListSettlements(SecurityUtil.getCurrentUserId(), status, page, size)));
    }
}
