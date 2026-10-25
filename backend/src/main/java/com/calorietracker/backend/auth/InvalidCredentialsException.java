package com.calorietracker.backend.auth;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Неверный email или пароль");
    }
}