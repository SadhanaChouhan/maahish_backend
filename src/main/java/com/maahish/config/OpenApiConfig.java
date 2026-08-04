package com.maahish.config;

import com.maahish.common.constants.AppConstants;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI maahishOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maahish API")
                        .description("""
                                Premium REST API for **%s** — %s.

                                | Module | Description |
                                |--------|-------------|
                                | Auth | Register, OTP, JWT login & refresh |
                                | Products | Search, filters, reviews, SEO slugs |
                                | Cart & Wishlist | Shopping experience |
                                | Orders | Checkout, Razorpay, tracking |
                                | Admin | Dashboard, catalog & user management |
                                """.formatted(AppConstants.BRAND_NAME, AppConstants.BRAND_TAGLINE))
                        .version("1.0.0")
                        .contact(new Contact()
                                .name(AppConstants.BRAND_NAME)
                                .email(AppConstants.SUPPORT_EMAIL)
                                .url(AppConstants.BRAND_WEBSITE_URL))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
