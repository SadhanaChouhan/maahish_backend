package com.maahish.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String imagePublicId;

    private Boolean isPrimary;
    private Integer sortOrder;
}
