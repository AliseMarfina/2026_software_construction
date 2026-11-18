package com.calorietracker.backend.diary.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateDiaryEntryRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("10000") @Digits(integer = 5, fraction = 2) BigDecimal grams,
        @NotNull @Pattern(regexp = "breakfast|lunch|dinner|snack", message = "допустимые значения: breakfast, lunch, dinner, snack") String mealType,
        OffsetDateTime eatenAt
) {
}
