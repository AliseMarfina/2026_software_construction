package com.calorietracker.backend.product.dto;

import com.calorietracker.backend.product.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String source,
        String barcode,
        String name,
        BigDecimal kcalPer100g,
        BigDecimal proteinPer100g,
        BigDecimal fatPer100g,
        BigDecimal carbsPer100g
) {

    public static ProductResponse from(Product product) {
        String source = product.getOwnerId() == null ? "openfoodfacts" : "custom";
        return new ProductResponse(
                product.getId(),
                source,
                product.getBarcode(),
                product.getName(),
                product.getKcalPer100g(),
                product.getProteinPer100g(),
                product.getFatPer100g(),
                product.getCarbsPer100g()
        );
    }
}