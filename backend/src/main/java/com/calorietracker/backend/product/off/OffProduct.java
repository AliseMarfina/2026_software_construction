package com.calorietracker.backend.product.off;

import java.math.BigDecimal;

public record OffProduct(
        String barcode,
        String name,
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs
) {
}