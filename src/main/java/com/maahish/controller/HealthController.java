package com.maahish.controller;

import com.maahish.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "UP",
                "application", "Maahish API",
                "version", "1.0.0"
        )));
    }
}
