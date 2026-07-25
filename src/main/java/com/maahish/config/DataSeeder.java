package com.maahish.config;

import com.maahish.entity.Category;
import com.maahish.entity.Product;
import com.maahish.entity.ProductImage;
import com.maahish.enums.ProductStatus;
import com.maahish.repository.CategoryRepository;
import com.maahish.repository.ProductRepository;
import com.maahish.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Database already seeded, skipping sample data");
                return;
            }

            log.info("Seeding sample categories and products...");
            Category maheshwari = categoryRepository.save(Category.builder()
                    .name("Maheshwari Sarees")
                    .slug("maheshwari-sarees")
                    .description("Authentic handwoven Maheshwari sarees")
                    .active(true)
                    .build());

            Category cottonSilk = categoryRepository.save(Category.builder()
                    .name("Cotton Silk")
                    .slug("cotton-silk")
                    .description("Premium cotton silk blends")
                    .active(true)
                    .build());

            seedProduct("Maheshwari Gold Border Saree", maheshwari, "Cotton Silk", "Gold",
                    new BigDecimal("4999"), new BigDecimal("10"), true, true);

            log.info("Seeded {} products", productRepository.count());
        };
    }

    private void seedProduct(String name, Category category, String fabric, String color,
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
                .fabric(fabric)
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
