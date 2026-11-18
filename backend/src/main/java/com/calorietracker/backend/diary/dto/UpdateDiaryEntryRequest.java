package com.calorietracker.backend.diary.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDiaryEntryRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("10000") @Digits(integer = 5, fraction = 2) BigDecimal grams
) {
}
