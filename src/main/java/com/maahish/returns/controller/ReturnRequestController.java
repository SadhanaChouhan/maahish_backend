package com.maahish.returns.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.returns.dto.request.CreateReturnRequestRequest;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.returns.dto.response.ReturnEligibilityResponse;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.service.ReturnRequestService;
import com.maahish.returns.dto.request.ReturnShipmentRequest;
import com.maahish.common.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/returns")
@RequiredArgsConstructor
@Tag(name = "Returns", description = "Customer return and exchange requests")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    @GetMapping("/eligibility")
    @Operation(summary = "Check return/exchange eligibility for an order item")
    public ResponseEntity<ApiResponse<ReturnEligibilityResponse>> checkEligibility(
            @RequestParam Long orderItemId) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.checkEligibility(SecurityUtil.getCurrentUserId(), orderItemId)));
    }

    @PostMapping("/upload-image")
    @Operation(summary = "Upload a return request image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                returnRequestService.uploadReturnImage(SecurityUtil.getCurrentUserId(), file)));
    }

    @PostMapping
    @Operation(summary = "Create a return or exchange request")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> create(
            @Valid @RequestBody CreateReturnRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                returnRequestService.createReturnRequest(SecurityUtil.getCurrentUserId(), request)));
    }

    @GetMapping
    @Operation(summary = "List my return requests")
    public ResponseEntity<ApiResponse<PageResponse<ReturnRequestResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.getCustomerReturns(SecurityUtil.getCurrentUserId(), page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.getCustomerReturn(SecurityUtil.getCurrentUserId(), id)));
    }

    @PostMapping("/{id}/shipment")
    @Operation(summary = "Submit courier shipment details after approval")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> submitShipment(
            @PathVariable Long id,
            @Valid @RequestPart("data") ReturnShipmentRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {
        return ResponseEntity.ok(ApiResponse.success(
                returnRequestService.submitShipment(SecurityUtil.getCurrentUserId(), id, request, receipt)));
    }
}
