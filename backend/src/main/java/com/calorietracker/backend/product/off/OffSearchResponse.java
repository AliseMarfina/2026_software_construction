package com.calorietracker.backend.product.off;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffSearchResponse(List<Item> hits) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String code,
            @JsonProperty("product_name") String productName,
            Nutriments nutriments
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nutriments(
            @JsonProperty("energy-kcal_100g") BigDecimal kcal,
            @JsonProperty("energy-kj_100g") BigDecimal kj,
            @JsonProperty("proteins_100g") BigDecimal protein,
            @JsonProperty("fat_100g") BigDecimal fat,
            @JsonProperty("carbohydrates_100g") BigDecimal carbs
    ) {
    }
}