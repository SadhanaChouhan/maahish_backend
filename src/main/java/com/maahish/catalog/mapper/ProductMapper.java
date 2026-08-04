package com.maahish.catalog.mapper;

import com.maahish.catalog.entity.Category;
import com.maahish.catalog.dto.response.CategoryResponse;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.entity.ProductImage;
import com.maahish.catalog.dto.response.ProductImageResponse;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.catalog.entity.Review;
import com.maahish.catalog.dto.response.ReviewResponse;
import com.maahish.catalog.util.InventoryStatusUtil;
import com.maahish.catalog.service.ProductStockService;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "fabric", source = "fabricType.name")
    @Mapping(target = "fabricTypeId", source = "fabricType.id")
    @Mapping(target = "primaryImageUrl", source = "product", qualifiedByName = "primaryImage")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "inventoryStatus", ignore = true)
    ProductSummaryResponse toSummary(Product product);

    List<ProductSummaryResponse> toSummaryList(List<Product> products);

    @Mapping(target = "images", source = "images")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "fabric", source = "fabricType.name")
    @Mapping(target = "fabricTypeId", source = "fabricType.id")
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "relatedProducts", ignore = true)
    @Mapping(target = "completeTheLook", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)
    ProductDetailResponse toDetail(Product product);

    @AfterMapping
    default void setInventoryStatus(Product product, @MappingTarget ProductSummaryResponse response) {
        response.setInventoryStatus(InventoryStatusUtil.resolve(ProductStockService.availableStock(product)));
    }

    @AfterMapping
    default void setInventoryStatus(Product product, @MappingTarget ProductDetailResponse response) {
        response.setInventoryStatus(InventoryStatusUtil.resolve(ProductStockService.availableStock(product)));
    }

    ProductImageResponse toImageResponse(ProductImage image);

    @Mapping(target = "userName", source = "user.name")
    ReviewResponse toReviewResponse(Review review);

    @Named("primaryImage")
    default String primaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .or(() -> product.getImages().stream()
                        .min(Comparator.comparing(ProductImage::getSortOrder)))
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }
}
