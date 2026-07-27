package com.maahish.catalog.dto.response;

import com.maahish.catalog.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String productCode;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal sellingPrice;
    private Integer stock;
    private String brand;
    private Long fabricTypeId;
    private String fabric;
    private String color;
    private String occasion;
    private BigDecimal rating;
    private Integer reviewCount;
    private String videoUrl;
    private String image360Url;
    private ProductStatus status;
    private Boolean bestSeller;
    private Boolean latestArrival;
    private CategoryResponse category;
    private ProductSellerDisplayResponse seller;
    private List<ProductImageResponse> images;
    private List<ReviewResponse> reviews;
    private List<ProductSummaryResponse> relatedProducts;
    private List<ProductSummaryResponse> completeTheLook;
    private LocalDateTime createdAt;
}
