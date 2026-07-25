package com.maahish.mapper;

import com.maahish.dto.response.*;
import com.maahish.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "primaryImageUrl", source = "product", qualifiedByName = "primaryImage")
    @Mapping(target = "category", source = "category")
    ProductSummaryResponse toSummary(Product product);

    List<ProductSummaryResponse> toSummaryList(List<Product> products);

    @Mapping(target = "images", source = "images")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "relatedProducts", ignore = true)
    @Mapping(target = "completeTheLook", ignore = true)
    ProductDetailResponse toDetail(Product product);

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
