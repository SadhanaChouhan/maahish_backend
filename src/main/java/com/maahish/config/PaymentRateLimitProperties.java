package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.payment")
public class PaymentRateLimitProperties {

    private int maxInitiateAttemptsPerHour = 10;
    private int maxVerifyAttemptsPerHour = 20;
}
