package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.sms")
public class SmsProperties {

    private boolean enabled = true;
    private String provider = "console";
    private String apiKey = "";
    private String senderId = "MAAHSH";
    private String countryCode = "91";
}
