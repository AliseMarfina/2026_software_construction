package com.calorietracker.backend.auth.dto;

public record LoginResponse(String accessToken, long expiresIn) {
}