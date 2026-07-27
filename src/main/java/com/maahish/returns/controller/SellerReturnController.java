package com.maahish.returns.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.service.ReturnRequestService;
import com.maahish.common.security.SecurityUtil;
import com.maahish.seller.entity.Seller;
import com.maahish.returns.dto.request.SellerExchangeReadyRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/seller/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Returns", description = "Seller return and exchange visibility")
public class SellerReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    @Operation(summary = "List return requests for seller products")
    public ResponseEntity<ApiResponse<PageResponse<ReturnRequestResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.getSellerReturns(SecurityUtil.getCurrentUserId(), page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.getSellerReturn(SecurityUtil.getCurrentUserId(), id)));
    }

    @PatchMapping("/{id}/exchange-ready")
    @Operation(summary = "Mark replacement product as ready")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> markExchangeReady(
            @PathVariable Long id,
            @Valid @RequestBody SellerExchangeReadyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.markExchangeReady(SecurityUtil.getCurrentUserId(), id, request)));
    }
}
