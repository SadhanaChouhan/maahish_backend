package com.maahish.service.impl;

import com.maahish.dto.request.ProductImageRequest;
import com.maahish.dto.request.ProductRequest;
import com.maahish.dto.request.ReviewRequest;
import com.maahish.dto.response.*;
import com.maahish.entity.*;
import com.maahish.enums.ProductStatus;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.ForbiddenException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.mapper.ProductMapper;
import com.maahish.mapper.SellerMapper;
import com.maahish.repository.*;
import com.maahish.service.CategoryService;
import com.maahish.service.CloudinaryService;
import com.maahish.service.ProductService;
import com.maahish.util.CodeGenerator;
import com.maahish.util.PageMapper;
import com.maahish.util.PaginationUtil;
import com.maahish.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;
    private final SellerMapper sellerMapper;

    @Override
    @Transactional(readOnly = true)
    public HomeResponse getHomePage() {
        Pageable pageable = PaginationUtil.createPageable(0, 20, "createdAt", "desc");
        List<ProductSummaryResponse> latest = productRepository
                .findByStatusAndLatestArrivalTrueOrderByCreatedAtDesc(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toSummary)
                .getContent();

        List<ProductSummaryResponse> bestSellers = productRepository
                .findByStatusAndBestSellerTrueOrderByRatingDescCreatedAtDesc(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toSummary)
                .getContent();

        return HomeResponse.builder()
                .tagline("Handwoven Heritage from the Banks of the Narmada")
                .brandStory("""
                        Maahish celebrates the timeless art of Maheshwari weaving from Madhya Pradesh. \
                        Each saree is handcrafted by skilled artisans using authentic cotton-silk blends, \
                        featuring distinctive zari borders and reversible pallus passed down through generations.""")
                .heritageYears(250)
                .artisanPartners(48)
                .trustBadges(List.of(
                        "100% Handloom Authentic",
                        "Artisan Direct Sourcing",
                        "Secure Razorpay Checkout",
                        "Pan-India Insured Delivery"
                ))
                .categories(categoryService.getActiveCategories())
                .latestArrivals(latest)
                .bestSellers(bestSellers)
                .occasions(List.of("Wedding", "Festival", "Office", "Casual", "Party"))
                .fabrics(List.of("Cotton Silk", "Pure Silk", "Cotton", "Tussar"))
                .colors(List.of("Red", "Green", "Blue", "Gold", "Maroon", "Pink", "White"))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> searchProducts(
            String keyword, Long categoryId, String fabric, String color, String occasion,
            BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortDir, int page, int size) {

        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);
        Page<Product> result = productRepository.searchProducts(
                keyword, categoryId, fabric, color, occasion, minPrice, maxPrice, sortBy, sortDir, pageable);
        return PageMapper.toPageResponse(result, productMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return buildProductDetail(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return buildProductDetail(product);
    }

    @Override
    @Transactional
    public void addReview(Long productId, Long userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .photoUrl(request.getPhotoUrl())
                .approved(true)
                .build();
        reviewRepository.save(review);
        updateProductRating(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProductForSeller(Seller seller, ProductRequest request) {
        Product product = mapSellerProductRequest(new Product(), request);
        product.setProductCode(CodeGenerator.generateProductCode());
        product.setSlug(generateUniqueSlug(request.getName()));
        product.setSellingPrice(calculateSellingPrice(request.getPrice(), request.getDiscount()));
        product.setSeller(seller);
        product.setBrand(seller.getBusinessName());
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        return buildProductDetail(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProductForSeller(Seller seller, Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }
        mapSellerProductRequest(product, request);
        product.setSellingPrice(calculateSellingPrice(request.getPrice(), request.getDiscount()));
        product.setBrand(seller.getBusinessName());
        product = productRepository.save(product);
        return buildProductDetail(product);
    }

    @Override
    @Transactional
    public void adminRemoveProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        deleteCloudinaryImages(product.getImages());
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProductForSeller(Seller seller, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }
        deleteCloudinaryImages(product.getImages());
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> adminSearchProducts(
            String keyword, Long categoryId, ProductStatus status,
            String sortBy, String sortDir, int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);
        Page<Product> result = productRepository.adminSearchProducts(
                keyword, categoryId, status, sortBy, sortDir, pageable);
        return PageMapper.toPageResponse(result, this::toSummaryWithSeller);
    }

    private ProductSummaryResponse toSummaryWithSeller(Product product) {
        ProductSummaryResponse summary = productMapper.toSummary(product);
        if (product.getSeller() != null) {
            summary.setSeller(sellerMapper.toSummary(product.getSeller()));
        }
        return summary;
    }

    private ProductDetailResponse buildProductDetail(Product product) {
        ProductDetailResponse detail = productMapper.toDetail(product);
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        List<Product> related = productRepository.findRelatedProducts(product.getId(), categoryId, 8);
        detail.setRelatedProducts(productMapper.toSummaryList(related));
        detail.setCompleteTheLook(productMapper.toSummaryList(related.stream().limit(4).toList()));

        Pageable reviewPage = PaginationUtil.createPageable(0, 10, "createdAt", "desc");
        List<ReviewResponse> reviews = reviewRepository
                .findByProductAndApprovedTrueOrderByCreatedAtDesc(product, reviewPage)
                .map(productMapper::toReviewResponse)
                .getContent();
        detail.setReviews(reviews);
        if (product.getSeller() != null) {
            Seller seller = product.getSeller();
            detail.setSeller(ProductSellerDisplayResponse.builder()
                    .businessName(seller.getBusinessName())
                    .ownerName(seller.getOwnerName())
                    .city(seller.getCity())
                    .state(seller.getState())
                    .businessLogoUrl(seller.getBusinessLogoUrl())
                    .platformOwned(seller.getPlatformOwned())
                    .build());
        }
        return detail;
    }

    private Product mapSellerProductRequest(Product product, ProductRequest request) {
        mapRequestToEntity(product, request);
        product.setBestSeller(Boolean.TRUE.equals(request.getBestSeller()));
        product.setLatestArrival(Boolean.TRUE.equals(request.getLatestArrival()));
        return product;
    }

    private Product mapRequestToEntity(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        product.setStock(request.getStock());
        product.setFabric(request.getFabric());
        product.setColor(request.getColor());
        product.setOccasion(request.getOccasion());
        product.setVideoUrl(request.getVideoUrl());
        product.setImage360Url(request.getImage360Url());
        if (request.getBestSeller() != null) product.setBestSeller(request.getBestSeller());
        if (request.getLatestArrival() != null) product.setLatestArrival(request.getLatestArrival());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getImages() != null) {
            syncProductImages(product, request.getImages());
        }
        return product;
    }

    private void syncProductImages(Product product, List<ProductImageRequest> imageRequests) {
        List<ProductImage> existingImages = new ArrayList<>(product.getImages());
        Set<String> retainedUrls = new HashSet<>();

        for (ProductImageRequest imgReq : imageRequests) {
            if (imgReq.getImageUrl() != null) {
                retainedUrls.add(imgReq.getImageUrl().trim());
            }
        }

        for (ProductImage existing : existingImages) {
            if (!retainedUrls.contains(existing.getImageUrl())) {
                cloudinaryService.deleteImage(existing.getImagePublicId());
            }
        }

        product.getImages().clear();
        int order = 0;
        for (ProductImageRequest imgReq : imageRequests) {
            if (imgReq.getImageUrl() == null || imgReq.getImageUrl().isBlank()) {
                continue;
            }
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imgReq.getImageUrl().trim())
                    .imagePublicId(resolveImagePublicId(imgReq, existingImages))
                    .isPrimary(imgReq.getIsPrimary() != null ? imgReq.getIsPrimary() : order == 0)
                    .sortOrder(imgReq.getSortOrder() != null ? imgReq.getSortOrder() : order++)
                    .build();
            product.getImages().add(image);
        }
    }

    private String resolveImagePublicId(ProductImageRequest request, List<ProductImage> existingImages) {
        if (request.getImagePublicId() != null && !request.getImagePublicId().isBlank()) {
            return request.getImagePublicId().trim();
        }
        for (ProductImage existing : existingImages) {
            if (existing.getImageUrl().equals(request.getImageUrl()) && existing.getImagePublicId() != null) {
                return existing.getImagePublicId();
            }
        }
        return cloudinaryService.extractPublicIdFromUrl(request.getImageUrl());
    }

    private void deleteCloudinaryImages(List<ProductImage> images) {
        for (ProductImage image : images) {
            cloudinaryService.deleteImage(image.getImagePublicId());
        }
    }

    private void updateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(
                product, PaginationUtil.createPageable(0, 1000, "createdAt", "desc")).getContent();
        if (reviews.isEmpty()) return;
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        product.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        product.setReviewCount(reviews.size());
        productRepository.save(product);
    }

    private BigDecimal calculateSellingPrice(BigDecimal price, BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        BigDecimal discountAmount = price.multiply(discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.subtract(discountAmount);
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
