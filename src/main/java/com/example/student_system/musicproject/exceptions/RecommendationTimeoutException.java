package com.example.student_system.musicproject.exceptions;

public class RecommendationTimeoutException extends RuntimeException {

    public RecommendationTimeoutException(String message) {
        super(message);
    }
}