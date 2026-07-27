package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.auth.cookie")
public class AuthCookieProperties {

    private boolean secure = false;
    private String sameSite = "Lax";
}
