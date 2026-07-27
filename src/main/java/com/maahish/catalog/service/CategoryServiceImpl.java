package com.maahish.catalog.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.catalog.entity.Category;
import com.maahish.catalog.repository.CategoryRepository;
import com.maahish.catalog.dto.request.CategoryRequest;
import com.maahish.catalog.dto.response.CategoryResponse;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(productMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(productMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = SlugUtil.toSlug(request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category name already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        return productMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.getName());
        category.setSlug(SlugUtil.toSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return productMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with linked products");
        }
        categoryRepository.delete(category);
    }
}
