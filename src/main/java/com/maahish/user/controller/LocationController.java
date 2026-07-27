package com.maahish.user.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.dto.response.LocationOptionResponse;
import com.maahish.user.service.LocationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<LocationOptionResponse>>> getCountries() {
        return ResponseEntity.ok(ApiResponse.success(locationService.getCountries()));
    }

    @GetMapping("/countries/{countryCode}/states")
    public ResponseEntity<ApiResponse<List<LocationOptionResponse>>> getStates(@PathVariable String countryCode) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getStates(countryCode)));
    }

    @GetMapping("/countries/{countryCode}/states/{stateCode}/districts")
    public ResponseEntity<ApiResponse<List<LocationOptionResponse>>> getDistricts(
            @PathVariable String countryCode,
            @PathVariable String stateCode) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getDistricts(countryCode, stateCode)));
    }
}
