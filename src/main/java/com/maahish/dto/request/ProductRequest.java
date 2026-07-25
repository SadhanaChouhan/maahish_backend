package com.maahish.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discount;

    @NotNull(message = "Stock is required")
    @Min(0)
    private Integer stock;

    private Long categoryId;

    private String fabric;
    private String color;
    private String occasion;
    private String videoUrl;
    private String image360Url;
    private Boolean bestSeller;
    private Boolean latestArrival;
    private List<ProductImageRequest> images;
}
