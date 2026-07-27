package com.maahish.config;

import com.maahish.catalog.entity.Category;
import com.maahish.catalog.entity.FabricType;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.entity.ProductImage;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.repository.CategoryRepository;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.common.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final FabricTypeRepository fabricTypeRepository;
    private final ProductRepository productRepository;

    @Bean
    @Order(3)
    CommandLineRunner seedSampleProduct() {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Database already has products, skipping sample product seed");
                return;
            }

            Category category = categoryRepository.findBySlug("maheshwari-sarees").orElse(null);
            FabricType fabricType = fabricTypeRepository.findByNameIgnoreCase("Cotton Silk").orElse(null);
            if (category == null || fabricType == null) {
                log.warn("Skipping sample product seed — master data not ready (category={}, fabricType={})",
                        category != null, fabricType != null);
                return;
            }

            log.info("Seeding sample product...");
            seedProduct("Maheshwari Gold Border Saree", category, fabricType, "Gold",
                    new BigDecimal("4999"), new BigDecimal("10"), true, true);
            log.info("Seeded {} products", productRepository.count());
        };
    }

    private void seedProduct(String name, Category category, FabricType fabricType, String color,
                             BigDecimal price, BigDecimal discount, boolean bestSeller, boolean latest) {
        BigDecimal selling = price.subtract(price.multiply(discount).divide(new BigDecimal("100")));
        String slug = SlugUtil.toSlug(name);
        String code = "MHS" + System.currentTimeMillis() % 100000;

        Product product = Product.builder()
                .productCode(code)
                .name(name)
                .slug(slug)
                .description("Exquisite handcrafted " + name + " from Maheshwar, Madhya Pradesh. Features traditional zari work and reversible pallu.")
                .price(price)
                .discount(discount)
                .sellingPrice(selling)
                .stock(25)
                .category(category)
                .fabricType(fabricType)
                .color(color)
                .occasion("Festival")
                .rating(new BigDecimal("4.5"))
                .reviewCount(12)
                .status(ProductStatus.ACTIVE)
                .bestSeller(bestSeller)
                .latestArrival(latest)
                .build();

        product.getImages().add(ProductImage.builder()
                .product(product)
                .imageUrl("https://placehold.co/600x800/e8d5b7/5c4033?text=" + slug)
                .isPrimary(true)
                .sortOrder(0)
                .build());

        productRepository.save(product);
    }
}
