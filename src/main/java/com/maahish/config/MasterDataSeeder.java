package com.maahish.config;

import com.maahish.catalog.entity.Category;
import com.maahish.catalog.entity.FabricType;
import com.maahish.catalog.repository.CategoryRepository;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.common.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class MasterDataSeeder {

    private final CategoryRepository categoryRepository;
    private final FabricTypeRepository fabricTypeRepository;

    @Bean
    @Order(1)
    CommandLineRunner seedMasterData() {
        return args -> {
            seedCategory("Maheshwari Sarees", "Authentic handwoven Maheshwari sarees");
            seedCategory("Maheshwari Dress Material", "Premium Maheshwari dress materials ");
            seedFabricType("Cotton Silk", "Classic Maheshwari cotton silk blend");
            seedFabricType("Pure Cotton", "Breathable pure cotton weave");
            seedFabricType("Pure Silk", "Luxurious pure silk");
            seedFabricType("Tissue", "Lightweight tissue fabric");
            seedFabricType("Garbh Rashmi", "Traditional Garbh Rashmi weave");
        };
    }

    private void seedCategory(String name, String description) {
        String slug = SlugUtil.toSlug(name);
        if (categoryRepository.existsBySlug(slug)) {
            return;
        }
        categoryRepository.save(Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .active(true)
                .build());
        log.info("Seeded category: {}", name);
    }

    private void seedFabricType(String name, String description) {
        String slug = SlugUtil.toSlug(name);
        if (fabricTypeRepository.existsBySlug(slug)) {
            return;
        }
        fabricTypeRepository.save(FabricType.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .active(true)
                .build());
        log.info("Seeded fabric type: {}", name);
    }
}
