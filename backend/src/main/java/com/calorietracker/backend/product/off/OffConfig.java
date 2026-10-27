package com.calorietracker.backend.product.off;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OffProperties.class)
public class OffConfig {

    @Bean
    public SlidingWindowRateLimiter offRateLimiter(OffProperties properties) {
        return new SlidingWindowRateLimiter(properties.maxSearchesPerMinute(), Duration.ofMinutes(1));
    }
}