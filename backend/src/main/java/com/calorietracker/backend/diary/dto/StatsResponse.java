package com.calorietracker.backend.diary.dto;

import com.calorietracker.backend.diary.DayTotals;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatsResponse(LocalDate from, LocalDate to, List<Day> days, Average average) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Day(LocalDate date, boolean noData, BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carbs) {

        public static Day empty(LocalDate date) {
            return new Day(date, true, null, null, null, null);
        }

        public static Day of(LocalDate date, DayTotals totals) {
            return new Day(date, false, totals.kcal(), totals.protein(), totals.fat(), totals.carbs());
        }
    }

    public record Average(BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carbs) {
    }
}
