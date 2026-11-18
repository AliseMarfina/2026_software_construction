package com.calorietracker.backend.diary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DayTotals(
        LocalDate date,
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs
) {
}
