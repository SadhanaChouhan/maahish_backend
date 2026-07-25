package com.maahish.config;

import com.maahish.constants.AppConstants;
import com.maahish.entity.Product;
import com.maahish.entity.Seller;
import com.maahish.entity.User;
import com.maahish.enums.SellerStatus;
import com.maahish.enums.UserRole;
import com.maahish.enums.UserStatus;
import com.maahish.repository.ProductRepository;
import com.maahish.repository.SellerRepository;
import com.maahish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PlatformSellerBootstrap {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner ensurePlatformSeller() {
        return args -> {
            Seller platformSeller = sellerRepository.findByPlatformOwnedTrue().orElseGet(this::createPlatformSeller);
            assignOrphanProducts(platformSeller);
        };
    }

    private Seller createPlatformSeller() {
        User user = userRepository.findByEmail(AppConstants.PLATFORM_SELLER_EMAIL).orElseGet(() -> {
            User platformUser = User.builder()
                    .name("Maahish Platform")
                    .email(AppConstants.PLATFORM_SELLER_EMAIL)
                    .mobile("9999999999")
                    .password(passwordEncoder.encode("platform-seller-not-for-login"))
                    .role(UserRole.ROLE_SELLER)
                    .status(UserStatus.INACTIVE)
                    .build();
            return userRepository.save(platformUser);
        });

        Seller seller = Seller.builder()
                .user(user)
                .businessName("Maahish")
                .ownerName("Maahish Platform")
                .email(AppConstants.PLATFORM_SELLER_EMAIL)
                .mobile("9999999999")
                .businessAddress("Maheshwar, Madhya Pradesh")
                .city("Maheshwar")
                .state("Madhya Pradesh")
                .pincode("451010")
                .bankAccountHolder("Maahish Platform")
                .bankAccountNumber("0000000000")
                .bankIfsc("MAHI0000001")
                .bankName("Maahish Platform Bank")
                .status(SellerStatus.ACTIVE)
                .platformOwned(true)
                .build();
        seller = sellerRepository.save(seller);
        log.info("Platform seller created for admin-managed products");
        return seller;
    }

    private void assignOrphanProducts(Seller platformSeller) {
        productRepository.findAll().forEach(product -> {
            if (product.getSeller() == null) {
                product.setSeller(platformSeller);
                productRepository.save(product);
            }
        });
    }
}
