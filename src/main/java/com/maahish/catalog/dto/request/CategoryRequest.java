package com.maahish.catalog.dto.request;

import com.maahish.catalog.entity.Category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private String imageUrl;
    private Boolean active;
}
