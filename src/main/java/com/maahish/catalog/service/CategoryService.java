package com.maahish.catalog.service;

import com.maahish.catalog.dto.request.CategoryRequest;
import com.maahish.catalog.dto.response.CategoryResponse;


import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getActiveCategories();

    List<CategoryResponse> getAllCategories();

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
