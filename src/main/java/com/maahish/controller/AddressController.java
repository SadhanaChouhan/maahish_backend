package com.maahish.controller;

import com.maahish.dto.request.AddressRequest;
import com.maahish.dto.response.AddressResponse;
import com.maahish.dto.response.ApiResponse;
import com.maahish.security.SecurityUtil;
import com.maahish.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Addresses", description = "Address management")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List user addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.getUserAddresses(SecurityUtil.getCurrentUserId())));
    }

    @PostMapping
    @Operation(summary = "Create address")
    public ResponseEntity<ApiResponse<AddressResponse>> create(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(SecurityUtil.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.updateAddress(SecurityUtil.getCurrentUserId(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressService.deleteAddress(SecurityUtil.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted"));
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Set default address")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.setDefault(SecurityUtil.getCurrentUserId(), id)));
    }
}
