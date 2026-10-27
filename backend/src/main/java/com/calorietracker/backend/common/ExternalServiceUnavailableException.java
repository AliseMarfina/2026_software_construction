package com.calorietracker.backend.common;

public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message) {
        super(message);
    }
}