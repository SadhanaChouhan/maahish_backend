package com.maahish.controller;

import com.maahish.constants.AppConstants;
import com.maahish.dto.request.AdminOrderStatusUpdateRequest;
import com.maahish.dto.request.CategoryRequest;
import com.maahish.dto.request.CommissionRuleRequest;
import com.maahish.dto.request.SellerStatusUpdateRequest;
import com.maahish.dto.request.SettlementStatusUpdateRequest;
import com.maahish.dto.response.*;
import com.maahish.enums.OrderStatus;
import com.maahish.enums.ProductStatus;
import com.maahish.enums.SellerStatus;
import com.maahish.enums.SettlementStatus;
import com.maahish.enums.UserStatus;
import com.maahish.service.AdminService;
import com.maahish.service.CategoryService;
import com.maahish.service.CommissionRuleService;
import com.maahish.service.OrderService;
import com.maahish.service.ProductService;
import com.maahish.service.SellerService;
import com.maahish.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard, products, orders, users, and categories")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final SellerService sellerService;
    private final CommissionRuleService commissionRuleService;
    private final SettlementService settlementService;

    // ── Dashboard ──────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Admin analytics dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    // ── Users ──────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List users with optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(status, page, size)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id)));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Update user status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, UserStatus> body) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateUserStatus(id, body.get("status"))));
    }

    // ── Products ───────────────────────────────────────────────────────────

    @GetMapping("/products")
    @Operation(summary = "List all products (includes discontinued)")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.adminSearchProducts(keyword, categoryId, status, sortBy, sortDir, page, size)));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Remove product from platform (policy violation or duplicate)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.adminRemoveProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed"));
    }

    @PatchMapping("/products/{id}/hide")
    @Operation(summary = "Hide a product that violates platform policies")
    public ResponseEntity<ApiResponse<Void>> hideProduct(@PathVariable Long id) {
        sellerService.adminHideProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product hidden"));
    }

    // ── Categories ─────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @Operation(summary = "List all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", categoryService.createCategory(request)));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted"));
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    @Operation(summary = "List all orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(status, page, size)));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get order by ID with seller-wise commission breakdown")
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAdminOrderDetail(id)));
    }

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Update order shipment status (admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminOrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated", orderService.adminUpdateOrderStatus(id, request)));
    }

    // ── Sellers ──────────────────────────────────────────────────────────────

    @GetMapping("/sellers")
    @Operation(summary = "List sellers with optional status and keyword filters")
    public ResponseEntity<ApiResponse<PageResponse<SellerSummaryResponse>>> getSellers(
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.adminSearchSellers(status, keyword, page, size)));
    }

    @GetMapping("/sellers/{id}")
    @Operation(summary = "Get seller details with payout summary")
    public ResponseEntity<ApiResponse<SellerResponse>> getSeller(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.adminGetSeller(id)));
    }

    @GetMapping("/sellers/{id}/orders")
    @Operation(summary = "List orders containing this seller's products")
    public ResponseEntity<ApiResponse<PageResponse<AdminSellerOrderSummaryResponse>>> getSellerOrders(
            @PathVariable Long id,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.adminGetSellerOrders(id, status, page, size)));
    }

    @GetMapping("/sellers/{id}/settlements")
    @Operation(summary = "List settlement and payout history for a seller")
    public ResponseEntity<ApiResponse<PageResponse<SellerSettlementResponse>>> getSellerSettlements(
            @PathVariable Long id,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementService.adminListSettlements(status, id, page, size)));
    }

    @PatchMapping("/sellers/{id}/status")
    @Operation(summary = "Approve, reject, activate, deactivate, or suspend a seller")
    public ResponseEntity<ApiResponse<SellerResponse>> updateSellerStatus(
            @PathVariable Long id,
            @Valid @RequestBody SellerStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Seller status updated", sellerService.adminUpdateSellerStatus(id, request)));
    }

    // ── Commission Rules ─────────────────────────────────────────────────────

    @GetMapping("/commission-rules")
    @Operation(summary = "List commission rules")
    public ResponseEntity<ApiResponse<List<CommissionRuleResponse>>> listCommissionRules() {
        return ResponseEntity.ok(ApiResponse.success(commissionRuleService.getAllRules()));
    }

    @PostMapping("/commission-rules")
    @Operation(summary = "Create commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> createCommissionRule(
            @Valid @RequestBody CommissionRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commission rule created", commissionRuleService.createRule(request)));
    }

    @PutMapping("/commission-rules/{id}")
    @Operation(summary = "Update commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> updateCommissionRule(
            @PathVariable Long id,
            @Valid @RequestBody CommissionRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Commission rule updated", commissionRuleService.updateRule(id, request)));
    }

    @PatchMapping("/commission-rules/{id}/enable")
    @Operation(summary = "Enable commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> enableCommissionRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Commission rule enabled", commissionRuleService.setEnabled(id, true)));
    }

    @PatchMapping("/commission-rules/{id}/disable")
    @Operation(summary = "Disable commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> disableCommissionRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Commission rule disabled", commissionRuleService.setEnabled(id, false)));
    }

    // ── Settlements ────────────────────────────────────────────────────────

    @GetMapping("/settlements")
    @Operation(summary = "List seller settlements")
    public ResponseEntity<ApiResponse<PageResponse<SellerSettlementResponse>>> listSettlements(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.adminListSettlements(status, sellerId, page, size)));
    }

    @PatchMapping("/settlements/{id}/status")
    @Operation(summary = "Update settlement status (manual payout)")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>> updateSettlementStatus(
            @PathVariable Long id,
            @Valid @RequestBody SettlementStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Settlement updated", settlementService.adminUpdateSettlementStatus(id, request)));
    }
}
