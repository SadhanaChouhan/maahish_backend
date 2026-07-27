package com.maahish.seller.service;

import com.maahish.admin.dto.response.AdminSellerOrderSummaryResponse;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.order.enums.OrderStatus;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.dto.request.ProductRequest;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.dto.response.SellerDashboardResponse;
import com.maahish.seller.dto.response.SellerOrderResponse;
import com.maahish.seller.dto.request.SellerProductPricingRequest;
import com.maahish.seller.dto.request.SellerProductStockRequest;
import com.maahish.seller.dto.request.SellerProfileUpdateRequest;
import com.maahish.seller.dto.request.SellerRegistrationRequest;
import com.maahish.seller.dto.response.SellerResponse;
import com.maahish.seller.enums.SellerStatus;
import com.maahish.seller.dto.request.SellerStatusUpdateRequest;
import com.maahish.seller.dto.response.SellerSummaryResponse;

import org.springframework.web.multipart.MultipartFile;

public interface SellerService {

    SellerResponse register(SellerRegistrationRequest request,
                            MultipartFile businessLogo,
                            MultipartFile profileImage);

    SellerDashboardResponse getDashboard(Long userId);

    SellerResponse getProfile(Long userId);

    SellerResponse updateProfile(Long userId,
                                 SellerProfileUpdateRequest request,
                                 MultipartFile businessLogo,
                                 MultipartFile profileImage);

    PageResponse<ProductSummaryResponse> getProducts(Long userId, int page, int size);

    ProductDetailResponse getProduct(Long userId, Long productId);

    ProductDetailResponse createProduct(Long userId, ProductRequest request);

    ProductDetailResponse updateProduct(Long userId, Long productId, ProductRequest request);

    void deleteProduct(Long userId, Long productId);

    ProductDetailResponse updateStock(Long userId, Long productId, SellerProductStockRequest request);

    ProductDetailResponse updatePricing(Long userId, Long productId, SellerProductPricingRequest request);

    FileUploadResponse uploadProductImage(Long userId, MultipartFile file);

    PageResponse<SellerOrderResponse> getOrders(Long userId, OrderStatus status, int page, int size);

    SellerOrderResponse getOrder(Long userId, Long orderId);

    void confirmOrder(Long userId, Long orderId);

    PageResponse<SellerSummaryResponse> adminSearchSellers(SellerStatus status, String keyword, int page, int size);

    SellerResponse adminGetSeller(Long sellerId);

    SellerResponse adminUpdateSellerStatus(Long sellerId, SellerStatusUpdateRequest request);

    PageResponse<AdminSellerOrderSummaryResponse> adminGetSellerOrders(Long sellerId, OrderStatus status, int page, int size);

    void adminHideProduct(Long productId);

    void assertSellerCanLogin(Seller seller);
}
