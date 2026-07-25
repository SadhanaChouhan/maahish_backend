package com.maahish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {

    private String tagline;
    private String heroImageUrl;
    private String brandStory;
    private Integer heritageYears;
    private Integer artisanPartners;
    private List<String> trustBadges;
    private List<CategoryResponse> categories;
    private List<ProductSummaryResponse> latestArrivals;
    private List<ProductSummaryResponse> bestSellers;
    private List<String> occasions;
    private List<String> fabrics;
    private List<String> colors;
}
