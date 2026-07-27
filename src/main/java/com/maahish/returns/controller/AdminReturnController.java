package com.maahish.returns.controller;

import com.maahish.returns.dto.request.AdminExchangeShipRequest;
import com.maahish.returns.dto.request.AdminReturnQualityCheckRequest;
import com.maahish.returns.dto.request.AdminReturnReviewRequest;
import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.service.ReturnRequestService;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.common.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Returns", description = "Admin return and exchange management")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    @Operation(summary = "List return requests")
    public ResponseEntity<ApiResponse<PageResponse<ReturnRequestResponse>>> list(
            @RequestParam(required = false) ReturnRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getAdminReturns(status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getAdminReturn(id)));
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Approve or reject a return request")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody AdminReturnReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.reviewReturn(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @PatchMapping("/{id}/receive-parcel")
    @Operation(summary = "Mark parcel as received")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> receiveParcel(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = body != null ? body.get("adminRemarks") : null;
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.markParcelReceived(SecurityUtil.getCurrentUserId(), id, remarks)));
    }

    @PatchMapping("/{id}/quality-check")
    @Operation(summary = "Complete quality inspection")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> qualityCheck(
            @PathVariable Long id,
            @Valid @RequestBody AdminReturnQualityCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.completeQualityCheck(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @PatchMapping("/{id}/process-refund")
    @Operation(summary = "Process Razorpay refund")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> processRefund(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.processRefund(SecurityUtil.getCurrentUserId(), id)));
    }

    @PatchMapping("/{id}/exchange-ship")
    @Operation(summary = "Dispatch exchange replacement")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> shipExchange(
            @PathVariable Long id,
            @Valid @RequestBody AdminExchangeShipRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.shipExchange(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @PatchMapping("/{id}/complete-exchange")
    @Operation(summary = "Mark exchange as completed")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> completeExchange(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.completeExchange(SecurityUtil.getCurrentUserId(), id)));
    }
}
