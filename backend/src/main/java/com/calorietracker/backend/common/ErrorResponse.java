package com.calorietracker.backend.common;

public record ErrorResponse(int status, String message) {
}