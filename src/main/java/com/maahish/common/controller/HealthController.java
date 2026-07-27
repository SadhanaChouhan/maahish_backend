package com.maahish.common.controller;

import com.maahish.common.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    @GetMapping
    @Operation(summary = "Health check (includes database connectivity via Actuator)")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        HealthComponent health = healthEndpoint.health();
        Status status = health.getStatus();

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", status.getCode());
        payload.put("application", "Maahish API");
        payload.put("version", "1.0.0");

        HttpStatus httpStatus = Status.UP.equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(ApiResponse.success(payload));
    }
}
