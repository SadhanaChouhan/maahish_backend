package com.maahish.mapper;

import com.maahish.dto.response.CategoryResponse;
import com.maahish.dto.response.ProductDetailResponse;
import com.maahish.dto.response.ProductImageResponse;
import com.maahish.dto.response.ProductSellerDisplayResponse;
import com.maahish.dto.response.ProductSummaryResponse;
import com.maahish.dto.response.ReviewResponse;
import com.maahish.dto.response.SellerSummaryResponse;
import com.maahish.entity.Category;
import com.maahish.entity.Product;
import com.maahish.entity.ProductImage;
import com.maahish.entity.Review;
import com.maahish.entity.Seller;
import com.maahish.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T21:32:42+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.slug( category.getSlug() );
        categoryResponse.description( category.getDescription() );
        categoryResponse.imageUrl( category.getImageUrl() );

        return categoryResponse.build();
    }

    @Override
    public ProductSummaryResponse toSummary(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductSummaryResponse.ProductSummaryResponseBuilder productSummaryResponse = ProductSummaryResponse.builder();

        productSummaryResponse.primaryImageUrl( primaryImage( product ) );
        productSummaryResponse.category( toCategoryResponse( product.getCategory() ) );
        productSummaryResponse.id( product.getId() );
        productSummaryResponse.productCode( product.getProductCode() );
        productSummaryResponse.name( product.getName() );
        productSummaryResponse.slug( product.getSlug() );
        productSummaryResponse.price( product.getPrice() );
        productSummaryResponse.discount( product.getDiscount() );
        productSummaryResponse.sellingPrice( product.getSellingPrice() );
        productSummaryResponse.fabric( product.getFabric() );
        productSummaryResponse.color( product.getColor() );
        productSummaryResponse.occasion( product.getOccasion() );
        productSummaryResponse.rating( product.getRating() );
        productSummaryResponse.reviewCount( product.getReviewCount() );
        productSummaryResponse.status( product.getStatus() );
        productSummaryResponse.stock( product.getStock() );
        productSummaryResponse.seller( sellerToSellerSummaryResponse( product.getSeller() ) );

        return productSummaryResponse.build();
    }

    @Override
    public List<ProductSummaryResponse> toSummaryList(List<Product> products) {
        if ( products == null ) {
            return null;
        }

        List<ProductSummaryResponse> list = new ArrayList<ProductSummaryResponse>( products.size() );
        for ( Product product : products ) {
            list.add( toSummary( product ) );
        }

        return list;
    }

    @Override
    public ProductDetailResponse toDetail(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDetailResponse.ProductDetailResponseBuilder productDetailResponse = ProductDetailResponse.builder();

        productDetailResponse.images( productImageListToProductImageResponseList( product.getImages() ) );
        productDetailResponse.category( toCategoryResponse( product.getCategory() ) );
        productDetailResponse.id( product.getId() );
        productDetailResponse.productCode( product.getProductCode() );
        productDetailResponse.name( product.getName() );
        productDetailResponse.slug( product.getSlug() );
        productDetailResponse.description( product.getDescription() );
        productDetailResponse.price( product.getPrice() );
        productDetailResponse.discount( product.getDiscount() );
        productDetailResponse.sellingPrice( product.getSellingPrice() );
        productDetailResponse.stock( product.getStock() );
        productDetailResponse.brand( product.getBrand() );
        productDetailResponse.fabric( product.getFabric() );
        productDetailResponse.color( product.getColor() );
        productDetailResponse.occasion( product.getOccasion() );
        productDetailResponse.rating( product.getRating() );
        productDetailResponse.reviewCount( product.getReviewCount() );
        productDetailResponse.videoUrl( product.getVideoUrl() );
        productDetailResponse.image360Url( product.getImage360Url() );
        productDetailResponse.status( product.getStatus() );
        productDetailResponse.bestSeller( product.getBestSeller() );
        productDetailResponse.latestArrival( product.getLatestArrival() );
        productDetailResponse.seller( sellerToProductSellerDisplayResponse( product.getSeller() ) );
        productDetailResponse.createdAt( product.getCreatedAt() );

        return productDetailResponse.build();
    }

    @Override
    public ProductImageResponse toImageResponse(ProductImage image) {
        if ( image == null ) {
            return null;
        }

        ProductImageResponse.ProductImageResponseBuilder productImageResponse = ProductImageResponse.builder();

        productImageResponse.id( image.getId() );
        productImageResponse.imageUrl( image.getImageUrl() );
        productImageResponse.imagePublicId( image.getImagePublicId() );
        productImageResponse.isPrimary( image.getIsPrimary() );
        productImageResponse.sortOrder( image.getSortOrder() );

        return productImageResponse.build();
    }

    @Override
    public ReviewResponse toReviewResponse(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewResponse.ReviewResponseBuilder reviewResponse = ReviewResponse.builder();

        reviewResponse.userName( reviewUserName( review ) );
        reviewResponse.id( review.getId() );
        reviewResponse.rating( review.getRating() );
        reviewResponse.comment( review.getComment() );
        reviewResponse.photoUrl( review.getPhotoUrl() );
        reviewResponse.createdAt( review.getCreatedAt() );

        return reviewResponse.build();
    }

    protected SellerSummaryResponse sellerToSellerSummaryResponse(Seller seller) {
        if ( seller == null ) {
            return null;
        }

        SellerSummaryResponse.SellerSummaryResponseBuilder sellerSummaryResponse = SellerSummaryResponse.builder();

        sellerSummaryResponse.id( seller.getId() );
        sellerSummaryResponse.businessName( seller.getBusinessName() );
        sellerSummaryResponse.ownerName( seller.getOwnerName() );
        sellerSummaryResponse.email( seller.getEmail() );
        sellerSummaryResponse.mobile( seller.getMobile() );
        sellerSummaryResponse.city( seller.getCity() );
        sellerSummaryResponse.state( seller.getState() );
        sellerSummaryResponse.status( seller.getStatus() );
        sellerSummaryResponse.businessLogoUrl( seller.getBusinessLogoUrl() );
        sellerSummaryResponse.createdAt( seller.getCreatedAt() );
        sellerSummaryResponse.platformOwned( seller.getPlatformOwned() );

        return sellerSummaryResponse.build();
    }

    protected List<ProductImageResponse> productImageListToProductImageResponseList(List<ProductImage> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductImageResponse> list1 = new ArrayList<ProductImageResponse>( list.size() );
        for ( ProductImage productImage : list ) {
            list1.add( toImageResponse( productImage ) );
        }

        return list1;
    }

    protected ProductSellerDisplayResponse sellerToProductSellerDisplayResponse(Seller seller) {
        if ( seller == null ) {
            return null;
        }

        ProductSellerDisplayResponse.ProductSellerDisplayResponseBuilder productSellerDisplayResponse = ProductSellerDisplayResponse.builder();

        productSellerDisplayResponse.businessName( seller.getBusinessName() );
        productSellerDisplayResponse.ownerName( seller.getOwnerName() );
        productSellerDisplayResponse.city( seller.getCity() );
        productSellerDisplayResponse.state( seller.getState() );
        productSellerDisplayResponse.businessLogoUrl( seller.getBusinessLogoUrl() );
        productSellerDisplayResponse.platformOwned( seller.getPlatformOwned() );

        return productSellerDisplayResponse.build();
    }

    private String reviewUserName(Review review) {
        User user = review.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getName();
    }
}
