package com.calorietracker.backend.diary.dto;

import com.calorietracker.backend.diary.DiaryEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record DiaryEntryResponse(
        Long id,
        String productName,
        BigDecimal grams,
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs,
        Instant loggedAt
) {

    public static DiaryEntryResponse from(DiaryEntry entry) {
        return new DiaryEntryResponse(
                entry.getId(),
                entry.getProductName(),
                entry.getGrams(),
                entry.getKcal(),
                entry.getProtein(),
                entry.getFat(),
                entry.getCarbs(),
                entry.getLoggedAt()
        );
    }
}