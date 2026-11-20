
package com.maahish.config;

import com.maahish.cart.entity.Cart;
import com.maahish.cart.repository.CartRepository;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner ensureAdminUser() {
        return args -> initializeAdmin();
    }

    @Transactional
    public void initializeAdmin() {

        if (!StringUtils.hasText(adminEmail)
                || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "Admin credentials are missing. Configure ADMIN_EMAIL and ADMIN_PASSWORD."
            );
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(
                user -> {
                    log.info("Admin account already exists. Skipping creation.");
                },
                () -> createAdmin(normalizedEmail)
        );
    }

    private void createAdmin(String email) {

        User admin = User.builder()
                .name("Administrator")
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        User savedAdmin = userRepository.save(admin);

        cartRepository.save(
                Cart.builder()
                        .user(savedAdmin)
                        .build()
        );

        log.info("Admin account created successfully.");
    }
}
