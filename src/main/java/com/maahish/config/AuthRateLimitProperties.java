package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.auth")
public class AuthRateLimitProperties {

    private int maxLoginAttemptsPerHour = 10;
    private int maxForgotPasswordAttemptsPerHour = 5;
    private int maxRegisterAttemptsPerHour = 5;
    private int maxSellerRegisterAttemptsPerHour = 3;
    private int maxVerifyOtpAttemptsPerHour = 10;
}
