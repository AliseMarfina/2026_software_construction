package com.calorietracker.backend.diary.dto;

import com.calorietracker.backend.diary.DiaryEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record DiaryEntryResponse(
        Long id,
        String productName,
        String mealType,
        BigDecimal grams,
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs,
        Instant eatenAt,
        Instant loggedAt,
        boolean productUpdated,
        Instant productUpdatedAt
) {

    public static DiaryEntryResponse from(DiaryEntry entry, Instant productUpdatedAt) {
        return new DiaryEntryResponse(
                entry.getId(),
                entry.getProductName(),
                entry.getMealType(),
                entry.getGrams(),
                entry.getKcal(),
                entry.getProtein(),
                entry.getFat(),
                entry.getCarbs(),
                entry.getEatenAt(),
                entry.getLoggedAt(),
                entry.isProductUpdated(),
                productUpdatedAt
        );
    }
}
