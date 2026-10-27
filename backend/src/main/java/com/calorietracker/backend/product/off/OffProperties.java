package com.calorietracker.backend.product.off;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.off")
public record OffProperties(
        String baseUrl,
        String userAgent,
        int pageSize,
        Duration timeout,
        int minDbResults,
        int maxSearchesPerMinute
) {
}