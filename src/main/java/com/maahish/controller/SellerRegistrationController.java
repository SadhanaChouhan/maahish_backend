package com.maahish.controller;

import com.maahish.dto.request.SellerRegistrationRequest;
import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.SellerResponse;
import com.maahish.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller Registration", description = "Public seller onboarding")
public class SellerRegistrationController {

    private final SellerService sellerService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register as a seller")
    public ResponseEntity<ApiResponse<SellerResponse>> register(
            @Valid @RequestPart("data") SellerRegistrationRequest request,
            @RequestPart(value = "businessLogo", required = false) MultipartFile businessLogo,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        SellerResponse response = sellerService.register(request, businessLogo, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Seller registration submitted. You will receive a confirmation email once reviewed by admin.",
                        response));
    }
}
