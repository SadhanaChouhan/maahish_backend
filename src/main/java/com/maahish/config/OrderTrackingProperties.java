package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.order-tracking")
public class OrderTrackingProperties {

    /** Max lookup attempts per order number per hour. */
    private int maxAttemptsPerHour = 30;

    /** Max lookup attempts per client IP per hour (curbs enumeration). */
    private int maxAttemptsPerIpPerHour = 20;
}
