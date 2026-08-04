package com.maahish.catalog.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.catalog.entity.Category;
import com.maahish.catalog.entity.FabricType;
import com.maahish.catalog.repository.CategoryRepository;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.common.util.CodeGenerator;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.catalog.dto.response.HomeResponse;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.entity.ProductImage;
import com.maahish.catalog.dto.request.ProductImageRequest;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.dto.request.ProductRequest;
import com.maahish.catalog.dto.response.ProductSellerDisplayResponse;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.catalog.entity.Review;
import com.maahish.catalog.repository.ReviewRepository;
import com.maahish.catalog.dto.request.ReviewRequest;
import com.maahish.catalog.dto.response.ReviewResponse;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.mapper.SellerMapper;
import com.maahish.common.util.SlugUtil;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.order.repository.OrderItemRepository;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

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
    private final FabricTypeRepository fabricTypeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final FabricTypeService fabricTypeService;
    private final CloudinaryService cloudinaryService;
    private final SellerMapper sellerMapper;
    private final ShoppingAccessValidator shoppingAccessValidator;
    private final OrderItemRepository orderItemRepository;

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
                .fabricTypes(fabricTypeService.getActiveFabricTypes())
                .occasions(List.of("Wedding", "Festival", "Office", "Casual", "Party"))
                .colors(List.of("Red", "Green", "Blue", "Gold", "Maroon", "Pink", "White"))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> searchProducts(
            String keyword, Long categoryId, Long fabricTypeId, String color, String occasion,
            BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortDir, int page, int size) {

        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);
        Page<Product> result = productRepository.searchProducts(
                keyword, categoryId, fabricTypeId, color, occasion, minPrice, maxPrice, sortBy, sortDir, pageable);
        return PageMapper.toPageResponse(result, productMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        assertVisibleToCustomers(product);
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
    @Transactional(readOnly = true)
    public ProductDetailResponse getCustomerProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        assertVisibleToCustomers(product);
        return buildProductDetail(product);
    }

    @Override
    @Transactional
    public void addReview(Long productId, Long userId, ReviewRequest request) {
        shoppingAccessValidator.requireCustomer(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        assertVisibleToCustomers(product);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        if (!orderItemRepository.existsDeliveredPurchaseByUserAndProduct(userId, productId)) {
            throw new BadRequestException(
                    "You can only review products from delivered orders in your account");
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

    private void assertVisibleToCustomers(Product product) {
        if (product.getStatus() == ProductStatus.INACTIVE
                || product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new ResourceNotFoundException("Product not found");
        }
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

        if (request.getFabricTypeId() != null) {
            FabricType fabricType = fabricTypeRepository.findById(request.getFabricTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fabric type not found"));
            product.setFabricType(fabricType);
        } else {
            product.setFabricType(null);
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
