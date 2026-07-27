package com.maahish.catalog.controller;

import com.maahish.catalog.dto.response.FabricTypeResponse;
import com.maahish.catalog.service.FabricTypeService;
import com.maahish.common.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/fabric-types")
@RequiredArgsConstructor
@Tag(name = "Fabric Types", description = "Active fabric type master data")
public class FabricTypeController {

    private final FabricTypeService fabricTypeService;

    @GetMapping
    @Operation(summary = "List active fabric types")
    public ResponseEntity<ApiResponse<List<FabricTypeResponse>>> getFabricTypes() {
        return ResponseEntity.ok(ApiResponse.success(fabricTypeService.getActiveFabricTypes()));
    }
}
