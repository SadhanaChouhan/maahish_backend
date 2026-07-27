package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.otp")
public class OtpProperties {

    private int expirationMinutes;
    private int length;
    private int maxVerifyAttempts = 5;
    private int resendCooldownSeconds = 60;
    private int maxSendsPerHour = 5;
}
