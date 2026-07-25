package com.maahish.controller;

import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.CategoryResponse;
import com.maahish.dto.response.HomeResponse;
import com.maahish.service.CategoryService;
import com.maahish.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Home page and categories")
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/home")
    @Operation(summary = "Get home page data")
    public ResponseEntity<ApiResponse<HomeResponse>> getHome() {
        return ResponseEntity.ok(ApiResponse.success(productService.getHomePage()));
    }

    @GetMapping("/categories")
    @Operation(summary = "List active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getActiveCategories()));
    }
}
