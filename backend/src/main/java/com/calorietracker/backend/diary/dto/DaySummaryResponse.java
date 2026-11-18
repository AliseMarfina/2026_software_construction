package com.calorietracker.backend.diary.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public record DaySummaryResponse(
        LocalDate date,
        BigDecimal totalKcal,
        BigDecimal totalProtein,
        BigDecimal totalFat,
        BigDecimal totalCarbs,
        List<DiaryEntryResponse> entries
) {

    public static DaySummaryResponse of(LocalDate date, List<DiaryEntryResponse> entries) {
        return new DaySummaryResponse(
                date,
                sum(entries, DiaryEntryResponse::kcal),
                sum(entries, DiaryEntryResponse::protein),
                sum(entries, DiaryEntryResponse::fat),
                sum(entries, DiaryEntryResponse::carbs),
                entries
        );
    }

    private static BigDecimal sum(List<DiaryEntryResponse> entries, Function<DiaryEntryResponse, BigDecimal> getter) {
        return entries.stream()
                .map(getter)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}