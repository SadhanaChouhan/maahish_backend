package com.maahish.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI maahishOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maahish API")
                        .description("""
                                Premium REST API for **Maahish** — authentic Maheshwari Cotton Silk Sarees.

                                | Module | Description |
                                |--------|-------------|
                                | Auth | Register, OTP, JWT login & refresh |
                                | Products | Search, filters, reviews, SEO slugs |
                                | Cart & Wishlist | Shopping experience |
                                | Orders | Checkout, Razorpay, tracking |
                                | Admin | Dashboard, catalog & user management |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Maahish")
                                .email("support@maahish.com"))
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
