package com.maahish.config;

import com.maahish.entity.Cart;
import com.maahish.entity.User;
import com.maahish.enums.UserRole;
import com.maahish.enums.UserStatus;
import com.maahish.repository.CartRepository;
import com.maahish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    public static final String ADMIN_EMAIL = "chouhansadhana17@gmail.com";
    public static final String ADMIN_PASSWORD = "12345";

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner ensureAdminUser() {
        return args -> {
            userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
                    user -> updateAdmin(user),
                    this::createAdmin
            );
        };
    }

    private void updateAdmin(User user) {
        user.setName("Sadhana Chouhan");
        user.setRole(UserRole.ROLE_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        userRepository.save(user);
    }

    private void createAdmin() {
        User admin = User.builder()
                .name("Sadhana Chouhan")
                .email(ADMIN_EMAIL)
                .mobile("7470857783")
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);
        cartRepository.save(Cart.builder().user(admin).build());
    }
}
