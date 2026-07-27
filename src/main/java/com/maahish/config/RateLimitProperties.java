package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.rate-limit")
public class RateLimitProperties {

    /**
     * {@code memory} for single-instance local dev; {@code redis} for production / horizontal scaling.
     */
    private String backend = "memory";
}
