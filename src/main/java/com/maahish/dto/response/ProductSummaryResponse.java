package com.maahish.dto.response;

import com.maahish.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private Long id;
    private String productCode;
    private String name;
    private String slug;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal sellingPrice;
    private String fabric;
    private String color;
    private String occasion;
    private BigDecimal rating;
    private Integer reviewCount;
    private ProductStatus status;
    private Integer stock;
    private String primaryImageUrl;
    private CategoryResponse category;
    private SellerSummaryResponse seller;
}
