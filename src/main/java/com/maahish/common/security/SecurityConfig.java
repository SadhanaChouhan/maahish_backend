package com.maahish.common.security;

import com.maahish.common.constants.AppConstants;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final Environment environment;

    private boolean isProductionProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    var registry = auth
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/health", "/actuator/health").permitAll();
                    if (isProductionProfile()) {
                        registry.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                                .denyAll()
                                .requestMatchers("/actuator/**").denyAll();
                    } else {
                        registry.requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                                .permitAll();
                    }
                    registry
                        .requestMatchers(HttpMethod.GET, "/v1/products/**", "/v1/categories/**", "/v1/fabric-types/**", "/v1/home").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/delivery/**", "/v1/locations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/orders/track").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/sellers/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/webhooks/**").permitAll()
                        .requestMatchers(AppConstants.ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers(AppConstants.SELLER_URLS).hasRole("SELLER")
                        .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
